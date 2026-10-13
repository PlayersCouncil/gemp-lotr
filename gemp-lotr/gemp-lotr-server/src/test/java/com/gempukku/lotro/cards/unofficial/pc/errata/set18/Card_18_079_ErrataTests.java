package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_079_ErrataTests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("frenzy", "68_79");

					// Three [orc] Orcs. The Scouting Orc (strength 8, vitality 2) and the unique Black Gate Sentry
					// (strength 12, vitality 3) have a skirmish ability used only on purpose; the Cutthroat Orc
					// is stronger for each companion of resistance 5 or less.
					put("scout", "15_116");
					put("sentry", "15_98");
					put("cutthroat", "11_113");
					// Half-troll of Far Harad: an [orc] minion who is no Orc; its text is an optional trigger at
					// the start of the assignment phase
					put("halftroll", "14_12");
					put("runner", "1_178");     // Goblin Runner: an Orc who is [moria]; cheated in, it does nothing

					// Rohan Worker: a follower that changes no number. Aid (1); its texts answer a transfer and
					// a skirmish its bearer wins.
					put("worker1", "15_135");
					put("worker2", "15_135");
					// Saruman, Servant of Sauron: an [isengard] follower, the Shadow player's own. Aid (1).
					put("saruman", "17_38");
					// Thrarin, Dwarven Smith: an ally, in the support area as a follower is
					put("thrarin", "1_27");
					put("armor", "1_8");        // Dwarven Armor: a possession a companion bears, as he bears a follower

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf, to bear the armor
					put("knight", "5_35");      // Gondorian Knight: strength 6, vitality 3; Knight is his whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FrenzyOfArrowsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Frenzy of Arrows
		 * Unique: false
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Make 2 [orc] Orcs gain <b>archer</b> until the regroup phase. Then add 1 to the minion
		 * 	archery total for each follower you can spot.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("frenzy");

		assertEquals("Frenzy of Arrows", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesTwoOrcCultureOrcsArchersAndAddsOneToTheMinionArcheryTotalForEachFollower() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var inSupport = scn.GetFreepsCard("worker1");
		var borne = scn.GetFreepsCard("worker2");
		var thrarin = scn.GetFreepsCard("thrarin");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(thrarin);
		// Three followers: one in the Free Peoples player's support area, one borne by a companion, and the
		// Shadow player's own in his support area. An ally and a possession are no followers.
		scn.MoveCardsToSupportArea(inSupport);
		scn.AttachCardsTo(aragorn, borne);
		scn.AttachCardsTo(gimli, armor);

		var frenzy = scn.GetShadowCard("frenzy");
		var scout = scn.GetShadowCard("scout");
		var sentry = scn.GetShadowCard("sentry");
		var cutthroat = scn.GetShadowCard("cutthroat");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(frenzy);
		scn.MoveCardsToSupportArea(saruman);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, sentry, cutthroat, halftroll, runner);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertInZone(Zone.SUPPORT, inSupport, saruman);
		assertInZone(Zone.ATTACHED, borne);
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertFalse(scn.HasKeyword(scout, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(sentry, Keyword.ARCHER));
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.ShadowPlayAvailable(frenzy));

		scn.ShadowPlayCard(frenzy);
		// Exactly two of the three [orc] Orcs. Not the Half-troll, an [orc] minion, nor the Goblin Runner, a
		// [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(scout, sentry, cutthroat));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(halftroll, runner));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(scout, sentry);
		assertTrue(scn.HasKeyword(scout, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(sentry, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(cutthroat, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(halftroll, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(runner, Keyword.ARCHER));
		// 2 archers + 3 followers
		assertEquals(5, scn.GetShadowArcheryTotal());
		assertEquals(0, scn.GetShadowHandCount());
		assertInZone(Zone.DISCARD, frenzy);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void IsAnArcheryEventAndWithOneOrcCultureOrcAndOneFollowerAddsOneForEach() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var borne = scn.GetFreepsCard("worker1");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, borne);

		var frenzy = scn.GetShadowCard("frenzy");
		var scout = scn.GetShadowCard("scout");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(frenzy);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, halftroll, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(5);
		// The Rohan Worker's Aid is offered at the start of the maneuver phase, borne or not.
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsPass();

		// The maneuver phase, with the same pool and the same board: no play.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(frenzy));

		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		scn.FreepsPass();
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.ShadowPlayAvailable(frenzy));

		// The only [orc] Orc there is becomes an archer; nobody else does.
		scn.ShadowPlayCard(frenzy);
		assertTrue(scn.HasKeyword(scout, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(halftroll, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(runner, Keyword.ARCHER));
		// 1 archer + 1 follower
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertInZone(Zone.DISCARD, frenzy);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void TheOrcsAreArchersUntilTheRegroupPhaseAndTheFollowersAddToThatArcheryPhaseOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var borne = scn.GetFreepsCard("worker1");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.AttachCardsTo(frodo, borne);

		var frenzy = scn.GetShadowCard("frenzy");
		var scout = scn.GetShadowCard("scout");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToHand(frenzy);

		scn.StartGame();
		scn.MoveMinionsToTable(scout, sentry);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.SetTwilight(5);
		scn.FreepsPass();

		// The two [orc] Orcs are the only ones to choose.
		scn.ShadowPlayCard(frenzy);
		assertTrue(scn.HasKeyword(scout, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(sentry, Keyword.ARCHER));
		// 2 archers + 1 follower
		assertEquals(3, scn.GetShadowArcheryTotal());

		// Three arrows land, and no fourth.
		scn.PassArcheryActions();
		scn.FreepsChooseCard(frodo);
		scn.FreepsChooseCard(frodo);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));

		// The archery phase is over: the follower's 1 has gone with it, and the Orcs are archers still.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(2, scn.GetShadowArcheryTotal());
		assertTrue(scn.HasKeyword(scout, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(sentry, Keyword.ARCHER));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, sentry}, new PhysicalCardImpl[]{knight, scout});
		// Aragorn (8) loses to the Sentry (12).
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// The last skirmish before the regroup phase.
		scn.FreepsResolveSkirmish(knight);
		assertTrue(scn.HasKeyword(scout, Keyword.ARCHER));
		assertTrue(scn.HasKeyword(sentry, Keyword.ARCHER));

		// The Knight (6) loses to the Scouting Orc (8), so both Orcs are still in play.
		scn.PassSkirmishActions();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, scout, sentry);
		assertFalse(scn.HasKeyword(scout, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(sentry, Keyword.ARCHER));
		assertEquals(0, scn.GetShadowArcheryTotal());
	}
}
