package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_255_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("spear", "4_255");
					put("spear2", "4_255");
					put("explorer", "4_250");   // Southron Explorer: Southron Man, vitality 3; a regroup ability only
					put("nomad", "7_132");      // Desert Nomad: Southron Man, vitality 2; damage +1 only at 2 threats
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man who is no Southron; +3 and fierce only at 2 burdens
					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] minion with no text

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4; a maneuver ability only
					put("boromir", "1_96");     // Boromir, Lord of Gondor: vitality 3; his text is about being overwhelmed
					put("bounder", "1_286");    // Bounder: Hobbit ally whose home is site 2 of this path
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SouthronSpearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Southron Spear
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a Southron.<br><b>Skirmish:</b> Remove (4) and exert bearer to wound a companion or ally bearer is skirmishing.
		*/

		var scn = GetScenario();

		var spear = scn.GetFreepsCard("spear");

		assertEquals("Southron Spear", spear.getBlueprint().getTitle());
		assertNull(spear.getBlueprint().getSubtitle());
		assertFalse(spear.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, spear.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, spear.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, spear.getBlueprint().getCardType());
		assertTrue(spear.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, spear.getBlueprint().getTwilightCost());
		assertEquals(2, spear.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnASouthronOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var spear = scn.GetShadowCard("spear");
		var explorer = scn.GetShadowCard("explorer");
		var nomad = scn.GetShadowCard("nomad");
		var axeman = scn.GetShadowCard("axeman");
		scn.MoveCardsToHand(spear);

		scn.StartGame();
		assertInHand(spear);
		scn.MoveMinionsToTable(explorer, nomad, axeman);
		scn.FreepsPass();

		assertInHand(spear);
		assertTrue(scn.ShadowPlayAvailable(spear));
		scn.ShadowPlayCard(spear);

		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, nomad));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(axeman));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(nomad);

		assertAttachedTo(spear, nomad);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesFourAndExertsBearerToWoundTheCompanionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var spear = scn.GetShadowCard("spear");
		var spear2 = scn.GetShadowCard("spear2");
		var explorer = scn.GetShadowCard("explorer");
		var nomad = scn.GetShadowCard("nomad");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, nomad, troop);
		scn.AttachCardsTo(explorer, spear);
		scn.AttachCardsTo(nomad, spear2);
		assertAttachedTo(spear, explorer);
		assertAttachedTo(spear2, nomad);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, explorer}, new PhysicalCardImpl[]{boromir, troop});
		// The second Southron, bearing the second Spear, goes to Aragorn as well.
		scn.ShadowAssignToMinions(aragorn, nomad);
		scn.SetTwilight(4);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(nomad));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowActionAvailable(spear));

		scn.ShadowUseCardAction(spear);

		// Its own bearer exerts, not the other Spear's; Boromir, in another skirmish, is no one the bearer skirmishes.
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(nomad));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingAnAllyItWoundsTheAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder);

		var spear = scn.GetShadowCard("spear");
		var explorer = scn.GetShadowCard("explorer");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, troop);
		scn.AttachCardsTo(explorer, spear);
		assertAttachedTo(spear, explorer);
		// The fellowship has moved to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bounder, explorer}, new PhysicalCardImpl[]{aragorn, troop});
		scn.SetTwilight(4);

		scn.FreepsResolveSkirmish(bounder);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowActionAvailable(spear));

		scn.ShadowUseCardAction(spear);

		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishingAnExhaustedCompanionTheWoundKillsHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var spear = scn.GetShadowCard("spear");
		var explorer = scn.GetShadowCard("explorer");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.AddWoundsToChar(boromir, 2);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable(explorer, troop);
		scn.AttachCardsTo(explorer, spear);
		assertAttachedTo(spear, explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, explorer}, new PhysicalCardImpl[]{aragorn, troop});
		scn.SetTwilight(4);

		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		// Vitality 3 with 2 wounds: exhausted, so a wound kills him where an exertion could not be made.
		assertEquals(4, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowActionAvailable(spear));

		scn.ShadowUseCardAction(spear);

		// His skirmish is over with him; Aragorn's is the one left.
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertInZone(Zone.DEAD, boromir);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void InAnotherMinionsSkirmishItIsPaidForAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var spear = scn.GetShadowCard("spear");
		var explorer = scn.GetShadowCard("explorer");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, troop);
		scn.AttachCardsTo(explorer, spear);
		assertAttachedTo(spear, explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, explorer}, new PhysicalCardImpl[]{boromir, troop});
		scn.SetTwilight(4);

		// Boromir's skirmish against the Troop first, while Aragorn waits for his against the bearer.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.ShadowActionAvailable(spear));

		scn.ShadowUseCardAction(spear);

		// Neither Boromir, skirmishing another minion, nor Aragorn, assigned to the bearer with his skirmish not begun.
		assertEquals(0, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithThreeTwilightInThePoolItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var spear = scn.GetShadowCard("spear");
		var explorer = scn.GetShadowCard("explorer");

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.AttachCardsTo(explorer, spear);
		assertAttachedTo(spear, explorer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.SetTwilight(3);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(explorer));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(spear));
	}

	@Test
	public void WithItsBearerExhaustedItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var spear = scn.GetShadowCard("spear");
		var explorer = scn.GetShadowCard("explorer");
		var nomad = scn.GetShadowCard("nomad");

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, nomad);
		scn.AttachCardsTo(explorer, spear);
		assertAttachedTo(spear, explorer);
		// Vitality 3 with 2 wounds: the bearer is exhausted; the other Southron in his skirmish could still exert.
		scn.AddWoundsToChar(explorer, 2);
		assertEquals(2, scn.GetWoundsOn(explorer));
		assertEquals(0, scn.GetWoundsOn(nomad));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, explorer);
		scn.ShadowAssignToMinions(aragorn, nomad);
		scn.SetTwilight(4);

		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(spear));
	}
}
