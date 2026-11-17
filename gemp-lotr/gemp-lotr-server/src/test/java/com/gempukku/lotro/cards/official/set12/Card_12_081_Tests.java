package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_081_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("evil", "12_81");
					put("evil2", "12_81");

					// Denizen of Khazad-dum and Denizen of the Black Pit: [orc] Orcs of strength 9 and 10, each +2 only at
					// an underground site, which none of these tests reaches
					put("denizen", "11_115");
					put("blackpit", "11_117");
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, strength 7; its text answers a weapon played on it
					// Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8; its one text is an optional
					// start-of-assignment play from the discard pile, which the skip into the assignment phase declines
					put("halftroll", "14_12");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					// Bounder: Hobbit ally, strength 2, vitality 2, home site 2, where the first move takes the fellowship
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void AbidingEvilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Abiding Evil
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot X burdens to make a character skirmishing an [orc] Orc strength -X.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("evil");

		assertEquals("Abiding Evil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void SpottingThreeBurdensMakesTheCompanionSkirmishingAnOrcOrcStrengthMinusThreeForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var evil = scn.GetShadowCard("evil");
		var denizen = scn.GetShadowCard("denizen");
		var blackpit = scn.GetShadowCard("blackpit");
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.MoveMinionsToTable(denizen, blackpit);

		// A skirmish event: not offered at the Shadow player's assignment actions.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(evil));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, denizen}, new PhysicalCardImpl[]{gimli, blackpit});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetBurdens());
		assertTrue(scn.ShadowPlayAvailable(evil));
		scn.ShadowPlayCard(evil);

		// X is the Shadow player's to name, from none to the 3 burdens there are.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("3");

		// Aragorn is the one character skirmishing an [orc] Orc, so nothing more is asked.  Gimli is assigned
		// to an [orc] Orc, but his skirmish has not begun.  The burdens are spotted, not removed.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(3, scn.GetBurdens());
		assertInZone(Zone.DISCARD, evil);
		assertInZone(Zone.SHADOW_CHARACTERS, denizen, blackpit);

		scn.FreepsPass();
		scn.ShadowPass();

		// 5 against the Denizen's 9: Aragorn loses, and with the skirmish over he is himself again.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnAllySkirmishingAnOrcOrcIsMadeWeakerByTheOneBurdenSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(bounder);

		var evil = scn.GetShadowCard("evil");
		var denizen = scn.GetShadowCard("denizen");
		scn.MoveCardsToHand(evil);

		scn.StartGame();
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(denizen);

		// The fellowship has moved to site 2, the Bounder's home.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bounder, denizen);
		scn.FreepsResolveSkirmish(bounder);
		scn.FreepsPass();

		assertEquals(2, scn.GetStrength(bounder));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.ShadowPlayAvailable(evil));
		scn.ShadowPlayCard(evil);

		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("1");

		assertEquals(1, scn.GetStrength(bounder));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, evil);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void PlayedInASkirmishAgainstAnOrcOfAnotherCultureOrAnOrcMinionWhoIsNoOrcItChangesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var evil = scn.GetShadowCard("evil");
		var evil2 = scn.GetShadowCard("evil2");
		var chieftain = scn.GetShadowCard("chieftain");
		var halftroll = scn.GetShadowCard("halftroll");
		scn.MoveCardsToHand(evil, evil2);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.MoveMinionsToTable(chieftain, halftroll);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, chieftain}, new PhysicalCardImpl[]{gimli, halftroll});

		// Aragorn against an Orc of [sauron]: the event is played, and nobody is weakened.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.ShadowPlayAvailable(evil));
		scn.ShadowPlayCard(evil);
		scn.ShadowChoose("2");

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.DISCARD, evil);
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(6, scn.GetStrength(gimli));

		scn.FreepsPass();
		scn.ShadowPass();

		// Gimli against an [orc] Half-troll.
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.ShadowPlayAvailable(evil2));
		scn.ShadowPlayCard(evil2);
		scn.ShadowChoose("2");

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertInZone(Zone.DISCARD, evil2);
		assertEquals(6, scn.GetStrength(gimli));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(2, scn.GetBurdens());
	}
}
