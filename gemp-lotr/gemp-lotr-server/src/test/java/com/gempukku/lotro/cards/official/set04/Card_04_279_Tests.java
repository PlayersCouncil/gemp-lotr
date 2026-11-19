package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_279_Tests
{

	// The Fellowship path. Its sites 3 (Rivendell Valley) and 6 (Galadriel's Glade) are sanctuaries, which ask for
	// healing only with a companion wounded, and none is here.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("helm", "4_279");

					put("guard", "5_83");       // Household Guard: [rohan] Man companion, valiant and nothing else
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T; a regroup ability used only on purpose
					put("firefoot", "13_126");  // Firefoot, Mearas of The Mark: [rohan] mount; its triggers need a token or Éomer
					put("snowmane", "13_136");  // Snowmane, Noble Mearas: [rohan] mount; its play trigger and ability need tokens and Théoden
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("brego", "4_263");      // Brego: [rohan] mount for a Man, Elf or Wizard; its play trigger is not met when cheated on
					put("armor", "1_92");       // Armor: [gondor] possession for a Man, armor and no mount
					put("merry", "103_85");     // Merry, Master Holbytla: [rohan] Hobbit; a Man only beside 3 [rohan] companions or Eowyn
					put("stablemaster", "15_134"); // Rohan Stable Master: [rohan] follower, "Bearer is considered to be mounted"

					put("filler1", "1_177");    // Goblin Patrol Troop: cards for a draw deck of a known size
					put("filler2", "1_177");
					put("filler3", "1_177");
					put("filler4", "1_177");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HelmHelmStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Helm! Helm!
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Regroup
		 * Game Text: <b>Regroup:</b> Spot 2 mounted [rohan] Men to liberate a site or draw 3 cards.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("helm");

		assertEquals("Helm! Helm!", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.REGROUP));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithBothOpenTheLiberationTakesTheHighestSiteAnOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var firefoot = scn.GetFreepsCard("firefoot");
		var snowmane = scn.GetFreepsCard("snowmane");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(herdsman);
		// Two mounted [rohan] Men: a companion and an ally.
		scn.AttachCardsTo(guard, firefoot);
		scn.AttachCardsTo(herdsman, snowmane);
		assertAttachedTo(firefoot, guard);
		assertAttachedTo(snowmane, herdsman);

		scn.StartGame();
		scn.SkipToSite(4);
		// Sites 1 to 3 are behind both markers: the opponent holds 1 and 2, the player himself 3.
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsControls(site3));

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetHand(VirtualTableScenario.P1, helm);
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"), scn.GetFreepsCard("filler4"));
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsPlayAvailable(helm));

		scn.FreepsPlayCard(helm);
		assertTrue(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertTrue(scn.FreepsChoiceAvailable("Liberate a site"));
		assertTrue(scn.FreepsChoiceAvailable("Draw 3 cards"));
		scn.FreepsChoose("Liberate a site");

		// Site 2, the highest an opponent controls, is back on the path; the player's own site 3 is passed over.
		assertFalse(scn.ShadowControls(site2));
		assertInZone(Zone.ADVENTURE_PATH, site2);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site3));
		// Nothing drawn.
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());
		assertInDiscard(helm);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteAnOpponentControlsThreeCardsAreDrawnUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var firefoot = scn.GetFreepsCard("firefoot");
		var snowmane = scn.GetFreepsCard("snowmane");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, firefoot);
		scn.AttachCardsTo(herdsman, snowmane);
		assertAttachedTo(firefoot, guard);
		assertAttachedTo(snowmane, herdsman);

		scn.StartGame();
		scn.SkipToSite(2);
		// The only controlled site is the player's own.
		scn.FreepsTakeControlOfSite();
		var site1 = scn.GetSite(1);
		assertTrue(scn.FreepsControls(site1));

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetHand(VirtualTableScenario.P1, helm);
		scn.SetFreepsDrawDeck(scn.GetFreepsCard("filler1"), scn.GetFreepsCard("filler2"), scn.GetFreepsCard("filler3"), scn.GetFreepsCard("filler4"));
		scn.FreepsRefreshDecision();
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(4, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsPlayAvailable(helm));

		scn.FreepsPlayCard(helm);
		// No site to liberate: the draw is the one branch, carried out unasked.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(1, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsControls(site1));
		assertInDiscard(helm);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithAnEmptyDrawDeckTheSiteIsLiberatedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var firefoot = scn.GetFreepsCard("firefoot");
		var snowmane = scn.GetFreepsCard("snowmane");
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(guard, firefoot);
		scn.AttachCardsTo(herdsman, snowmane);
		assertAttachedTo(firefoot, guard);
		assertAttachedTo(snowmane, herdsman);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		var site1 = scn.GetSite(1);
		assertTrue(scn.ShadowControls(site1));

		scn.SkipToPhase(Phase.REGROUP);
		scn.SetHand(VirtualTableScenario.P1, helm);
		scn.SetFreepsDrawDeck();
		scn.FreepsRefreshDecision();
		assertEquals(0, scn.GetFreepsDeckCount());
		assertTrue(scn.FreepsPlayAvailable(helm));

		scn.FreepsPlayCard(helm);
		// Three cards cannot be drawn: the liberation is the one branch, carried out unasked.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertFalse(scn.ShadowControls(site1));
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertEquals(0, scn.GetFreepsHandCount());
		assertInDiscard(helm);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void CannotBePlayedWithOneMountedRohanManBesideNearMisses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var helm = scn.GetFreepsCard("helm");
		var guard = scn.GetFreepsCard("guard");
		var firefoot = scn.GetFreepsCard("firefoot");
		var herdsman = scn.GetFreepsCard("herdsman");
		var armor = scn.GetFreepsCard("armor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var brego = scn.GetFreepsCard("brego");
		var merry = scn.GetFreepsCard("merry");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		scn.MoveCompanionsToTable(guard, aragorn, merry);
		scn.MoveAlliesToTable(herdsman);
		// One mounted [rohan] Man (the Guard); a [rohan] Man bearing a possession that is no mount (the Herdsman);
		// a mounted Man of [gondor] (Aragorn); a mounted [rohan] Hobbit (Merry, two [rohan] companions only).
		scn.AttachCardsTo(guard, firefoot);
		scn.AttachCardsTo(herdsman, armor);
		scn.AttachCardsTo(aragorn, brego);
		scn.AttachCardsTo(merry, stablemaster);
		assertAttachedTo(firefoot, guard);
		assertAttachedTo(armor, herdsman);
		assertAttachedTo(brego, aragorn);
		assertAttachedTo(stablemaster, merry);
		scn.MoveCardsToHand(helm);
		assertInHand(helm);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertInHand(helm);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(helm));
	}
}
