package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_01_061_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("songs", "1_61");

					// Hosts of the Last Alliance: [elven] tale, a condition to the support area; its modifier wants an
					// Elf companion and its response an Elf about to be wounded, neither of which is in play
					put("hosts", "2_18");
					// Alliance Reforged: [elven] tale, a Fellowship event; with no Elf companion to spot it draws nothing
					put("alliance", "4_58");
					// Shoulder to Shoulder: an [elven] condition to the support area that is no tale; a maneuver ability only
					put("shoulder", "1_59");
					// Stairs of Khazad-dum: a [dwarven] tale to the support area; a modifier for Dwarves against [moria] minions
					put("stairs", "1_24");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SongsoftheBlessedRealmStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Songs of the Blessed Realm
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: Plays to your support area.<br>Each time you play an [elven] tale, you may remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("songs");

		assertEquals("Songs of the Blessed Realm", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachElvenTalePlayedOffersToRemoveOneBurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var songs = scn.GetFreepsCard("songs");
		var hosts = scn.GetFreepsCard("hosts");
		var alliance = scn.GetFreepsCard("alliance");
		scn.MoveCardsToSupportArea(songs);
		scn.MoveCardsToHand(hosts, alliance);

		scn.StartGame();
		scn.AddBurdens(3);

		assertEquals(3, scn.GetBurdens());

		// An [elven] tale that is a condition.
		scn.FreepsPlayCard(hosts);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(songs));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, hosts);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Each time: an [elven] tale that is an event, played after it, is offered again.
		scn.FreepsPlayCard(alliance);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(songs));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertInZone(Zone.DISCARD, alliance);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElvenCardThatIsNoTaleOrATaleOfAnotherCultureIsNotAnsweredAndTheOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var songs = scn.GetFreepsCard("songs");
		var shoulder = scn.GetFreepsCard("shoulder");
		var stairs = scn.GetFreepsCard("stairs");
		var hosts = scn.GetFreepsCard("hosts");
		scn.MoveCardsToHand(songs, shoulder, stairs, hosts);

		scn.StartGame();
		scn.AddBurdens(2);

		assertEquals(2, scn.GetBurdens());

		// Songs itself, played from hand, goes to the support area; it is an [elven] condition and no tale, and
		// answers nothing of its own play.
		scn.FreepsPlayCard(songs);
		assertInZone(Zone.SUPPORT, songs);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(songs));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another [elven] card that is no tale.
		scn.FreepsPlayCard(shoulder);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(songs));
		assertInZone(Zone.SUPPORT, shoulder);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A tale that is not [elven].
		scn.FreepsPlayCard(stairs);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(songs));
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(2, scn.GetBurdens());

		// An [elven] tale: offered, and declined, nothing changes.
		scn.FreepsPlayCard(hosts);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(songs));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertInZone(Zone.SUPPORT, hosts);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoBurdenToRemoveAnElvenTaleIsPlayedWithNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var songs = scn.GetFreepsCard("songs");
		var hosts = scn.GetFreepsCard("hosts");
		scn.MoveCardsToSupportArea(songs);
		scn.MoveCardsToHand(hosts);

		scn.StartGame();

		assertEquals(0, scn.GetBurdens());
		scn.FreepsPlayCard(hosts);

		// Removing a burden would do nothing, and the trigger costs nothing: the offer is withheld (Q-0224).
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(songs));
		assertInZone(Zone.SUPPORT, hosts);
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
