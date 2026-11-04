package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ax", "18_84");
					put("ax2", "18_84");
					put("goblin", "11_137");     // Scurrying Goblin: [orc] Orc, strength 5, vitality 1; its play trigger wants an underground site
					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc, strength 9, vitality 2; +2 only at an underground site
					put("unyielding", "11_142"); // Unyielding Goblin: [orc] Orc, strength 5, vitality 2; its skirmish ability wants an underground site
					put("halftroll", "14_12");   // Half-troll of Far Harad: an [orc] minion who is no Orc; a start-of-assignment trigger
					put("runner", "1_178");      // Goblin Runner: a [moria] Orc; its trigger is on being played
					put("warg", "17_92");        // Vicious Warg: [orc] mount, strength +4, vitality +2; "Bearer is fierce" and nothing else

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only
					put("gimli", "1_13");        // Gimli, Son of Gloin: strength 6, vitality 3, damage +1; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishAxStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Orkish Ax
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be an [orc] Orc.<br>Each time bearer wins a skirmish, you may add a threat.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ax");

		assertEquals("Orkish Ax", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnAnOrcOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ax = scn.GetShadowCard("ax");
		var goblin = scn.GetShadowCard("goblin");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(ax);
		assertInHand(ax);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, denizen, halftroll, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(ax));
		scn.ShadowPlayCard(ax);
		// Two [orc] Orcs of other titles and strengths; an [orc] Half-troll and a [moria] Orc are not offered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(goblin, denizen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(halftroll, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(denizen);

		assertAttachedTo(ax, denizen);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeBearerWinsASkirmishItsOwnAxMayAddAThreat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ax = scn.GetShadowCard("ax");
		var ax2 = scn.GetShadowCard("ax2");
		var goblin = scn.GetShadowCard("goblin");
		var denizen = scn.GetShadowCard("denizen");
		var warg = scn.GetShadowCard("warg");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		// Three companions, so that threats can be added.
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin, denizen);
		// The Warg makes the Goblin fierce, so that he skirmishes, and wins, a second time in the turn.
		scn.AttachCardsTo(goblin, ax, warg);
		scn.AttachCardsTo(denizen, ax2);
		assertAttachedTo(ax, goblin);
		assertAttachedTo(warg, goblin);
		assertAttachedTo(ax2, denizen);
		assertEquals(0, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, goblin}, new PhysicalCardImpl[]{gimli, denizen});

		// The Denizen, bearing the other Ax, wins first: 9 + 2 against Gimli's 6.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(11, scn.GetStrength(denizen));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ax2));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ax));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetThreats());

		// The Goblin wins: 5 + 2 + 4 against Aragorn's 8.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(11, scn.GetStrength(goblin));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ax));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ax2));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetThreats());

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, goblin);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();

		// The second win offers it again.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(ax));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWhenBearerLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ax = scn.GetShadowCard("ax");
		var unyielding = scn.GetShadowCard("unyielding");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(unyielding);
		scn.AttachCardsTo(unyielding, ax);
		assertAttachedTo(ax, unyielding);
		assertEquals(0, scn.GetThreats());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, unyielding);
		scn.FreepsResolveSkirmish(aragorn);
		// 5 + 2 against Aragorn's 8: the bearer loses, and with vitality 2 lives through the wound.
		assertEquals(7, scn.GetStrength(unyielding));
		assertEquals(0, scn.GetWoundsOn(unyielding));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(unyielding));
		assertAttachedTo(ax, unyielding);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ax));
		assertEquals(0, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NotOfferedForAWinWhenNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ax = scn.GetShadowCard("ax");
		var goblin = scn.GetShadowCard("goblin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		// As many threats as companions: no more can be added.
		scn.AddThreats(3);
		assertEquals(3, scn.GetThreats());
		scn.MoveMinionsToTable(goblin);
		scn.AttachCardsTo(goblin, ax);
		assertAttachedTo(ax, goblin);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, goblin);
		scn.FreepsResolveSkirmish(gimli);
		// 5 + 2 against Gimli's 6: a win, short of double his strength.
		assertEquals(7, scn.GetStrength(goblin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(ax));
		assertEquals(3, scn.GetThreats());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
