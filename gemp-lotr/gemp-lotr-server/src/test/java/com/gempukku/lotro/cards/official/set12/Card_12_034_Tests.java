package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_034_Tests
{

	// A Shadows-block path, whose sites take their numbers as they are laid: the Shadow player lays each site the
	// fellowship moves to, and either player may replace a site with any site of his own adventure deck.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("leader", "12_34");
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: a [gandalf] Wizard companion; his text is about his strength
					// Saruman, The White: a [gandalf] Wizard who is an ally; his abilities are used only on purpose.
					put("whitesaruman", "32_24");
					put("avenger", "6_27");       // Ent Avenger: a [gandalf] companion who is no Wizard
					// Saruman, Keeper of Isengard: a Wizard minion who is not [gandalf]; nothing here is an Uruk-hai.
					put("keeper", "3_68");
					put("savage", "1_151");       // Uruk Savage: a minion with damage +1 and nothing else
				}},
				new HashMap<>() {{
					put("glade", "11_239");     // Fangorn Glade: adds (1) for each Free Peoples weapon when moved to
					put("ruins", "18_138");     // Sirannon Ruins: River, no text
					put("crags", "11_234");     // Crags of Emyn Muil: wounds on a regroup move from it in region 2
					put("nurn", "17_148");      // Nurn: its text is about controlling it
					put("angle", "12_185");     // The Angle: a Shadow ability that plays a Nazgul
					put("homestead", "11_230"); // Buckland Homestead: a Shadow ability that plays a Nazgul
					put("emynmuil", "12_187");  // Emyn Muil: a Maneuver ability for a minion
					put("rauros", "17_146");    // Falls of Rauros: acts when moved to in region 2
					put("mineshaft", "13_185"); // Abandoned Mine Shaft: acts on a regroup move from it outside region 1
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void TraveledLeaderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Traveled Leader
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver or Regroup
		 * Game Text: Spot a [gandalf] Wizard to replace a site in the fellowship's current region with a site from
		 * 	your adventure deck.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("leader");

		assertEquals("Traveled Leader", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void InTheManeuverPhaseASiteOfTheCurrentRegionIsReplacedWithOneFromTheFreePeoplesAdventureDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leader = scn.GetFreepsCard("leader");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(leader);
		var savage = scn.GetShadowCard("savage");

		var glade = scn.GetFreepsSite("glade");
		var ruins = scn.GetShadowSite("ruins");
		var crags = scn.GetShadowSite("crags");
		var nurn = scn.GetShadowSite("nurn");
		var angle = scn.GetFreepsSite("angle");
		var homestead = scn.GetFreepsSite("homestead");

		// Two moves in the first turn: the Shadow player lays Sirannon Ruins as site 2 and Crags of Emyn Muil as
		// site 3, all three sites in region 1.
		scn.StartGame(glade);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.ShadowChooseCardBPFromSelection(crags);
		scn.MoveMinionsToTable(savage);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.MANEUVER);
		// The next site already lies ahead, as a card that plays the fellowship's next site would lay it: Nurn,
		// site 4, in region 2.
		scn.MoveCardToAdventurePath(nurn);

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertSame(glade, scn.GetSite(1));
		assertSame(ruins, scn.GetSite(2));
		assertSame(crags, scn.GetSite(3));
		assertSame(nurn, scn.GetSite(4));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(leader));

		scn.FreepsPlayCard(leader);
		// The three sites of region 1, his own and the opponent's, the fellowship's current site among them; not
		// the site of region 2.
		assertTrue(scn.FreepsHasCardChoiceAvailable(glade, ruins, crags));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(nurn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(ruins);

		// Any site of his adventure deck may take its place.
		assertTrue(scn.FreepsHasBPChoice(angle));
		assertTrue(scn.FreepsHasBPChoice(homestead));
		scn.FreepsChooseCardBPFromSelection(angle);

		// His own The Angle is site 2 now, and the Shadow player's Sirannon Ruins is back in its owner's adventure deck.
		assertSame(angle, scn.GetSite(2));
		assertInZone(Zone.ADVENTURE_PATH, angle);
		assertInZone(Zone.ADVENTURE_DECK, ruins, homestead);
		assertSame(glade, scn.GetSite(1));
		assertSame(crags, scn.GetSite(3));
		assertSame(nurn, scn.GetSite(4));
		// The fellowship has not moved.
		assertSame(crags, scn.GetCurrentSite());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInDiscard(leader);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InTheRegroupPhaseAWizardAllyIsSpottedAndTheFellowshipsCurrentSiteIsReplaced() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leader = scn.GetFreepsCard("leader");
		var whitesaruman = scn.GetFreepsCard("whitesaruman");
		scn.MoveAlliesToTable(whitesaruman);
		scn.MoveCardsToHand(leader);

		var glade = scn.GetFreepsSite("glade");
		var ruins = scn.GetShadowSite("ruins");
		var angle = scn.GetFreepsSite("angle");

		scn.StartGame(glade);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		// With no minion played the turn goes from the Shadow phase to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);

		assertSame(ruins, scn.GetCurrentSite());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		// A [gandalf] Wizard who is an ally is the only one to spot.
		assertTrue(scn.FreepsPlayAvailable(leader));

		scn.FreepsPlayCard(leader);
		assertTrue(scn.FreepsHasCardChoiceAvailable(glade, ruins));
		scn.FreepsChooseCard(ruins);
		scn.FreepsChooseCardBPFromSelection(angle);

		// The fellowship is at the new site 2, his own The Angle.
		assertSame(angle, scn.GetSite(2));
		assertSame(angle, scn.GetCurrentSite());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertInZone(Zone.ADVENTURE_DECK, ruins);
		assertSame(glade, scn.GetSite(1));
		assertInDiscard(leader);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithoutAGandalfWizard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var leader = scn.GetFreepsCard("leader");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(leader);
		var keeper = scn.GetShadowCard("keeper");

		var glade = scn.GetFreepsSite("glade");
		var ruins = scn.GetShadowSite("ruins");

		scn.StartGame(glade);
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(ruins);
		scn.MoveMinionsToTable(keeper);
		scn.ShadowRefreshDecision();
		scn.SkipToPhase(Phase.MANEUVER);

		// A [gandalf] companion who is no Wizard, and a Wizard minion who is not [gandalf].
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(leader));
		assertInZone(Zone.HAND, leader);
		assertSame(ruins, scn.GetSite(2));
	}
}
