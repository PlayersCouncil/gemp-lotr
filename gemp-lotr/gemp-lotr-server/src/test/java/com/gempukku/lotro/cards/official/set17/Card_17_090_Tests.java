package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_090_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gear", "17_90");
					put("gear2", "17_90");

					// Vicious Warg: [orc] mount, on an [orc] Orc of strength 9 or less; bearer is fierce
					put("vicious", "17_92");
					put("vicious2", "17_92");

					// Minions
					put("denizen", "11_115");     // Denizen of Khazad-dum: [orc] Orc, strength 9 (+2 at an underground site)
					put("mountainorc", "11_129"); // Mountain Orc: [orc] Orc, strength 9; its trigger exerts a companion of resistance 5 or less
					put("strategist", "12_97");   // Orc Strategist: [orc] Orc, strength 11; lurker, a maneuver ability
					put("halftroll", "14_12");    // Half-troll of Far Harad: an [orc] minion who is no Orc
					put("runner", "1_178");       // Goblin Runner: a [moria] Orc; its trigger is on being played

					// Free Peoples cards
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, resistance 6
					put("defend", "4_115");       // Defend It and Hope: Maneuver: exert a [gondor] Man to wound a minion
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: only spotted, for Long I Fell
					put("fell", "4_97");          // Long I Fell: Skirmish: the Shadow player may make you wound a minion of your choice
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RidersGearStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Rider's Gear
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Armor
		 * Strength: +2
		 * Game Text: Bearer must be an [orc] Orc.<br>While bearer is mounted, it cannot take wounds except during the skirmish phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gear");

		assertEquals("Rider's Gear", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayedOnlyOnAnOrcOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gear = scn.GetShadowCard("gear");
		var denizen = scn.GetShadowCard("denizen");
		var strategist = scn.GetShadowCard("strategist");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(gear);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(denizen, strategist, halftroll, runner);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(gear));
		scn.ShadowPlayCard(gear);
		// [orc] Orcs of two titles are offered; an [orc] minion who is no Orc and an Orc who is not [orc] are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(denizen, strategist));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(halftroll, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(strategist);

		assertAttachedTo(gear, strategist);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void MountedBearerCannotBeWoundedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var gear = scn.GetShadowCard("gear");
		var gear2 = scn.GetShadowCard("gear2");
		var vicious = scn.GetShadowCard("vicious");
		var vicious2 = scn.GetShadowCard("vicious2");
		var denizen = scn.GetShadowCard("denizen");
		var mountainorc = scn.GetShadowCard("mountainorc");
		var strategist = scn.GetShadowCard("strategist");

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, mountainorc, strategist);
		// The bearer, mounted; another mounted [orc] Orc without the Gear; and a bearer of a second Gear, with
		// no mount.
		scn.AttachCardsTo(denizen, gear, vicious);
		scn.AttachCardsTo(mountainorc, vicious2);
		scn.AttachCardsTo(strategist, gear2);
		assertAttachedTo(gear, denizen);
		assertAttachedTo(vicious, denizen);
		assertAttachedTo(vicious2, mountainorc);
		assertAttachedTo(gear2, strategist);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(mountainorc));
		assertEquals(0, scn.GetWoundsOn(strategist));
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(defend);
		// The mounted bearer may not be wounded; a mounted Orc who does not bear the Gear may, and so may a
		// bearer of the Gear who is not mounted.
		assertTrue(scn.FreepsHasCardChoiceAvailable(mountainorc, strategist));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(denizen));
		scn.FreepsChooseCard(strategist);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(strategist));
		assertEquals(0, scn.GetWoundsOn(mountainorc));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void MountedBearerInNoSkirmishTakesAWoundDuringAnothersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(fell);

		var gear = scn.GetShadowCard("gear");
		var vicious = scn.GetShadowCard("vicious");
		var denizen = scn.GetShadowCard("denizen");
		var mountainorc = scn.GetShadowCard("mountainorc");

		scn.StartGame();
		scn.MoveMinionsToTable(denizen, mountainorc);
		scn.AttachCardsTo(denizen, gear, vicious);
		assertAttachedTo(gear, denizen);
		assertAttachedTo(vicious, denizen);
		assertEquals(0, scn.GetWoundsOn(denizen));
		scn.SkipToAssignments();
		// Aragorn (resistance 6) skirmishes Mountain Orc; the mounted bearer is in no skirmish at all.
		scn.FreepsAssignToMinions(aragorn, mountainorc);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(mountainorc));

		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		// The wound comes during the skirmish phase, so the mounted bearer is a candidate beside the other minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(denizen, mountainorc));
		scn.FreepsChooseCard(denizen);

		assertEquals(1, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(mountainorc));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
