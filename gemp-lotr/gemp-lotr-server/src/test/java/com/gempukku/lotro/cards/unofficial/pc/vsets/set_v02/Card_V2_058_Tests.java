package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.CantTakeWoundsModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_058_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dead", "102_58");

					// Twilight minions.
					// Ulaire Nelya, Ringwraith in Twilight: a Nazgul of the [ringwraith] culture, cost 5, site 2;
					// his text needs 2 burdens or 2 wounds on the Ring-bearer.
					put("nelya", "2_84");
					// Ulaire Attea, The Easterling: a Nazgul; "when you play" text only, and he is never played.
					put("attea", "2_82");
					put("lights", "6_99");      // Corpse Lights: [sauron] Wraith, cost 3, site 4; a skirmish ability
					// Dead Ones: [sauron] Wraith, cost 1, site 4; to play, remove a burden or spot a twilight minion.
					put("ones", "6_100");

					// Minions that are not twilight.
					// Dead Faces: a Wraith of the [ringwraith] culture, cost 2, site 3; its trigger needs it
					// assigned to a companion.
					put("faces", "16_4");
					// Ulaire Lemenya, Lieutenant of Morgul: a Nazgul; weakens bearers of [ringwraith] conditions.
					put("lemenya", "1_232");
					put("soldier", "1_271");    // Orc Soldier: [sauron] Orc; a skirmish ability

					// Cover of Darkness, Omen of Despair: a [sauron] condition with the twilight keyword; its
					// ability needs a Troll.
					put("omen", "103_93");

					// Dead Man of Dunharrow: a [gondor] companion who is a Wraith.  Exerts when played, and
					// here he is never played.
					put("deadman", "10_27");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DownToJoinTheDeadOnesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Down to Join the Dead Ones
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: To play, spot a Wraith.
		 * 	Regroup: Discard two twilight minions to add a burden. The Free Peoples player may wound the
		 * 	Ring-bearer to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dead");

		assertEquals("Down to Join the Dead Ones", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilAWraithIsSpottedAndThenPlaysToTheSupportAreaFor1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetShadowCard("dead");
		var nelya = scn.GetShadowCard("nelya");
		var soldier = scn.GetShadowCard("soldier");
		var ones = scn.GetShadowCard("ones");
		scn.MoveCardsToHand(dead, ones);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya, soldier);
		// 2 set + 1 companion + 2 for site 2
		scn.SetTwilight(2);
		scn.FreepsPass();

		// A twilight Nazgul of the [ringwraith] culture and an Orc of [sauron]: neither is a Wraith.
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.HasKeyword(nelya, Keyword.TWILIGHT));
		assertEquals(Culture.WRAITH, nelya.getBlueprint().getCulture());
		assertFalse(scn.ShadowPlayAvailable(dead));

		// Dead Ones, a Wraith: 1 to play + 2 roaming, spotting the twilight Nazgul.
		scn.ShadowPlayCard(ones);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, ones);
		assertTrue(scn.ShadowPlayAvailable(dead));

		scn.ShadowPlayCard(dead);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, dead);
		assertNotAttachedTo(dead, ones);
		assertNotAttachedTo(dead, nelya);
		assertNotAttachedTo(dead, soldier);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AWraithWithoutTheTwilightKeywordAndOfAnotherCultureIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetShadowCard("dead");
		var faces = scn.GetShadowCard("faces");
		scn.MoveCardsToHand(dead);

		scn.StartGame();
		scn.MoveMinionsToTable(faces);
		scn.FreepsPass();

		// 1 companion + 2 for site 2
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.HasKeyword(faces, Keyword.TWILIGHT));
		assertTrue(scn.ShadowPlayAvailable(dead));

		scn.ShadowPlayCard(dead);
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, dead);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayersWraithCompanionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetShadowCard("dead");
		var soldier = scn.GetShadowCard("soldier");
		var deadman = scn.GetFreepsCard("deadman");
		scn.MoveCompanionsToTable(deadman);
		scn.MoveCardsToHand(dead);

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.FreepsPass();

		// 2 companions + 2 for site 2.  The one Wraith on the table is a [gondor] companion.
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(dead));

		scn.ShadowPlayCard(dead);
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, dead);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DiscardsTwoTwilightMinionsToAddABurdenWhenTheFreePeoplesPlayerDeclines() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var dead = scn.GetShadowCard("dead");
		var omen = scn.GetShadowCard("omen");
		var nelya = scn.GetShadowCard("nelya");
		var lights = scn.GetShadowCard("lights");
		var ones = scn.GetShadowCard("ones");
		var faces = scn.GetShadowCard("faces");
		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(dead, omen);
		scn.MoveMinionsToTable(nelya, lights, ones, faces, lemenya);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.ShadowActionAvailable(dead));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.ShadowUseCardAction(dead);
		// A twilight Nazgul and two twilight Wraiths
		assertTrue(scn.ShadowHasCardChoiceAvailable(nelya, lights, ones));
		// A Wraith and a Nazgul that are not twilight, and a twilight condition
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(faces, lemenya, omen));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());

		scn.ShadowChooseCards(nelya, lights);
		assertInZone(Zone.DISCARD, nelya, lights);
		assertInZone(Zone.SHADOW_CHARACTERS, ones, faces, lemenya);
		assertInZone(Zone.SUPPORT, dead, omen);

		// The minions are gone and the burden is not yet there when the Free Peoples player is asked.
		assertTrue(scn.FreepsDecisionAvailable("wound the Ring-bearer"));
		assertEquals(0, scn.GetBurdens());
		scn.FreepsChooseNo();

		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayWoundTheRingBearerToPreventTheBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var dead = scn.GetShadowCard("dead");
		var nelya = scn.GetShadowCard("nelya");
		var attea = scn.GetShadowCard("attea");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(dead);
		scn.MoveMinionsToTable(nelya, attea);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(dead));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));

		// Two twilight minions and two to discard: no choice is put.
		scn.ShadowUseCardAction(dead);
		assertInZone(Zone.DISCARD, nelya, attea);

		assertTrue(scn.FreepsDecisionAvailable("wound the Ring-bearer"));
		scn.FreepsChooseYes();

		// Frodo takes the wound, not Sam, who is Ring-bound and not the Ring-bearer.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		// No burden, and the minions stay discarded.
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, nelya, attea);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOnlyOneTwilightMinionAmongOtherMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dead = scn.GetShadowCard("dead");
		var omen = scn.GetShadowCard("omen");
		var nelya = scn.GetShadowCard("nelya");
		var faces = scn.GetShadowCard("faces");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(dead, omen);
		// One twilight minion; a Wraith and a Nazgul that are not twilight; a twilight condition; and a
		// second twilight minion in hand.
		scn.MoveMinionsToTable(nelya, faces, lemenya);
		scn.MoveCardsToHand(lights);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertTrue(scn.HasKeyword(omen, Keyword.TWILIGHT));
		assertInZone(Zone.SHADOW_CHARACTERS, nelya, faces, lemenya);
		assertFalse(scn.ShadowActionAvailable(dead));
	}

	@Test
	public void WithARingBearerWhoCannotTakeWoundsNobodyIsAskedAndTheBurdenIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);

		var dead = scn.GetShadowCard("dead");
		var nelya = scn.GetShadowCard("nelya");
		var attea = scn.GetShadowCard("attea");

		scn.StartGame();
		scn.ApplyAdHocModifier(new CantTakeWoundsModifier(null, null, frodo));
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveCardsToSupportArea(dead);
		scn.MoveMinionsToTable(nelya, attea);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(dead));
		assertEquals(0, scn.GetBurdens());

		// Sam could take a wound; the Ring-bearer cannot, so there is nothing to offer.
		scn.ShadowUseCardAction(dead);
		assertInZone(Zone.DISCARD, nelya, attea);
		assertEquals(1, scn.GetBurdens());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
