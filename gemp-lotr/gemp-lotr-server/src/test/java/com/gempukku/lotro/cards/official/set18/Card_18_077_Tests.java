package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("whisper", "18_77");
					// Elendil's Army: a unique [gondor] follower.  Its Aid exerts a [gondor] companion, and none is laid,
					// so no Aid is offered at the start of the maneuver phase.
					put("army", "18_47");
					// Rohan Worker: a [rohan] follower; its texts answer a transfer and a skirmish won.
					put("worker", "15_135");
					// Saruman, Servant of Sauron: an [isengard] follower, the Shadow player's own.
					put("saruman", "17_38");

					// Warrior of Dunland: a [men] Man minion; ambush, and another ambush at a plains site.
					put("warrior", "11_103");
					// Rampaging Easterling: a [men] Man minion, lurker; a trigger on another [men] minion winning.
					put("easterling", "11_98");
					// Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit and no Man.  His maneuver ability
					// liberates a site the Shadow player controls, and he controls none.
					put("ted", "18_75");
					// Dunlending Brigand: a [dunland] Man minion with no text.
					put("brigand", "4_10");

					// Gimli, Son of Glóin: an unbound [dwarven] Dwarf; a skirmish ability never used.
					put("gimli", "1_13");
					// Legolas, Greenleaf: an unbound [elven] Elf; an archery ability never used.
					put("legolas", "1_50");
					// Ent Avenger: an unbound [gandalf] Ent, vitality 4; a strength bonus only with 3 Ents.
					put("avenger", "6_27");
					// Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; a fellowship ability never used.
					put("sam", "1_311");
					// Bounder: a Hobbit ally; a skirmish ability never used.
					put("bounder", "1_286");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhisperintheDarkStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Whisper in the Dark
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype:
		 * Game Text: Bearer must be a follower.<br>At the start of the maneuver phase, if you can spot a [men] Man, the Free Peoples player must exert an unbound companion.
		*/

		var scn = GetScenario();

		var whisper = scn.GetFreepsCard("whisper");

		assertEquals("Whisper in the Dark", whisper.getBlueprint().getTitle());
		assertNull(whisper.getBlueprint().getSubtitle());
		assertFalse(whisper.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, whisper.getBlueprint().getSide());
		assertEquals(Culture.MEN, whisper.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, whisper.getBlueprint().getCardType());
		assertEquals(0, whisper.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedOnAFollowerOfEitherPlayerAndOnNoCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var army = scn.GetFreepsCard("army");
		var worker = scn.GetFreepsCard("worker");
		var gimli = scn.GetFreepsCard("gimli");
		var bounder = scn.GetFreepsCard("bounder");
		var whisper = scn.GetShadowCard("whisper");
		var saruman = scn.GetShadowCard("saruman");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(army, worker, saruman);
		scn.MoveCardsToHand(whisper);

		scn.StartGame();
		assertInZone(Zone.HAND, whisper);
		assertInZone(Zone.SUPPORT, army, worker, saruman);
		scn.FreepsPass();
		scn.MoveMinionsToTable(warrior);
		scn.ShadowRefreshDecision();

		assertTrue(scn.ShadowPlayAvailable(whisper));
		scn.ShadowPlayCard(whisper);

		// Followers of two cultures in the Free Peoples player's support area, and the Shadow player's own; no
		// companion, ally or minion.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(army, worker, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, gimli, bounder, warrior));
		scn.ShadowChooseCard(worker);

		assertAttachedTo(whisper, worker);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseWithAMenManTheFreePeoplesPlayerExertsAnUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var army = scn.GetFreepsCard("army");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var avenger = scn.GetFreepsCard("avenger");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var whisper = scn.GetShadowCard("whisper");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCompanionsToTable(gimli, legolas, avenger, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(army);
		scn.AttachCardsTo(army, whisper);

		scn.StartGame();
		assertAttachedTo(whisper, army);
		// Vitality 4 with 3 wounds: the Ent Avenger cannot exert.
		scn.AddWoundsToChar(avenger, 3);
		assertEquals(3, scn.GetWoundsOn(avenger));
		scn.MoveMinionsToTable(warrior);
		scn.SkipToPhase(Phase.MANEUVER);

		// Two unbound companions of other cultures and races; not the exhausted one, not Sam (Ring-bound), not
		// Frodo, and not the ally.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(avenger, sam, frodo, bounder));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsChooseCard(gimli);

		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The start of the archery phase sets nothing off.
		scn.PassCurrentPhaseActions();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
	}

	@Test
	public void NothingHappensWithAMenMinionWhoIsNoManAndAManOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var whisper = scn.GetShadowCard("whisper");
		var ted = scn.GetShadowCard("ted");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCompanionsToTable(gimli, legolas);
		scn.MoveCardsToSupportArea(army);
		scn.AttachCardsTo(army, whisper);

		scn.StartGame();
		assertAttachedTo(whisper, army);
		scn.MoveMinionsToTable(ted, brigand);
		scn.SkipToPhase(Phase.MANEUVER);

		// This condition is itself the [men] card that is no Man.
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
	}

	@Test
	public void WithNoUnboundCompanionNobodyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var army = scn.GetFreepsCard("army");
		var sam = scn.GetFreepsCard("sam");
		var whisper = scn.GetShadowCard("whisper");
		var warrior = scn.GetShadowCard("warrior");
		scn.MoveCompanionsToTable(sam);
		scn.MoveCardsToSupportArea(army);
		scn.AttachCardsTo(army, whisper);

		scn.StartGame();
		assertAttachedTo(whisper, army);
		scn.MoveMinionsToTable(warrior);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(sam));
	}

	@Test
	public void ExertsAgainAtTheStartOfASecondManeuverPhaseInTheTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var army = scn.GetFreepsCard("army");
		var gimli = scn.GetFreepsCard("gimli");
		var legolas = scn.GetFreepsCard("legolas");
		var whisper = scn.GetShadowCard("whisper");
		var easterling = scn.GetShadowCard("easterling");
		scn.MoveCompanionsToTable(gimli, legolas);
		scn.MoveCardsToSupportArea(army);
		scn.AttachCardsTo(army, whisper);

		scn.StartGame();
		assertAttachedTo(whisper, army);
		scn.MoveMinionsToTable(easterling);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(legolas));
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The Easterling is taken off so that no skirmish is fought, and laid again after the second move.
		scn.MoveCardsToDiscard(easterling);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveMinionsToTable(easterling);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, legolas));
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
