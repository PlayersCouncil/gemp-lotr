package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_133_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tactician", "13_133");
					put("tactician2", "13_133");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion; Valiant is its whole text
					// Doom Drove Them: a [rohan] support-area condition that holds [rohan] tokens; its two abilities
					// are used only on purpose
					put("doom1", "8_86");
					put("doom2", "8_86");
					// Garrison of Osgiliath: a [gondor] support-area condition that holds [gondor] tokens; a skirmish
					// ability only
					put("garrison", "6_52");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The card's own block: the Shadow player lays each next site, and holds others to replace it with.
	protected VirtualTableScenario GetReplacedSiteScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tactician", "13_133");
					put("guard", "5_83");           // Household Guard: a [rohan] Man; Valiant is its whole text
					// Úlairë Nelya, Third of the Nine Riders: "Shadow: Exert Úlairë Nelya to replace the
					// fellowship's current site with a site from your adventure deck."  Fierce, vitality 3.
					put("nelya", "11_222");
					put("marksman1", "1_176");      // Goblin Marksman: Archer is its whole text
					put("marksman2", "1_176");
					put("marksman3", "1_176");
				}},
				// site1 is Fangorn Glade, site2 Abandoned Mine Shaft, site5 Sirannon Ruins: none does anything
				// on the first move of a game or in an archery phase
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	// The Fellowship block's numbered path, where Úlairë Nelya 1_233 replaces an opponent's site by its number
	protected VirtualTableScenario GetFellowshipPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tactician", "13_133");
					put("guard", "5_83");           // Household Guard: a [rohan] Man companion; Valiant is its whole text
					// Úlairë Nelya, Lieutenant of Morgul: "Shadow: Exert Úlairë Nelya and spot an opponent's site to
					// replace it with your site of the same number."  Fierce, vitality 3.
					put("nelya", "1_233");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	protected VirtualTableScenario GetShadowsPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tactician", "13_133");
					put("guard", "5_83");           // Household Guard: a [rohan] Man companion; Valiant is its whole text
					// Rohirrim Herdsman: a [rohan] Man ally (home 3T); a regroup ability used only on purpose
					put("herdsman", "7_247");
					// Merry, Master Holbytla: a [rohan] companion who is a Hobbit, and a Man only beside 3 [rohan]
					// companions or Éowyn; his trigger answers a [rohan] Man exerting
					put("merry", "103_85");
					put("faramir", "12_47");        // Faramir, Dúnadan of Gondor: a [gondor] Man; a skirmish ability only
					// There's Another Way: "Regroup: Discard this condition to replace the fellowship's current site
					// with a site from your adventure deck", the one Free Peoples card that does
					put("way", "12_40");
					put("nelya", "11_222");         // Úlairë Nelya: "Shadow: Exert Úlairë Nelya to replace the fellowship's current site ..."
					put("marksman1", "1_176");      // Goblin Marksman: Archer is its whole text
					put("marksman2", "1_176");
					put("marksman3", "1_176");
				}},
				// site1 Fangorn Glade, site2 Abandoned Mine Shaft and site5 Sirannon Ruins: none does anything on
				// the first move of a game, in a Shadow or archery phase, or before a regroup move
				VirtualTableScenario.ShadowsSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Shadows
		);
	}

	@Test
	public void RiddermarkTacticianStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Riddermark Tactician
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: When you play this, you may reinforce a [rohan] token.<br>Each time a Shadow player replaces the fellowship's current site, you may spot another [rohan] Man to make, until the regroup phase, the fellowship archery total +2 and the minion archery total -2.
		*/

		var scn = GetScenario();

		var tactician = scn.GetFreepsCard("tactician");

		assertEquals("Riddermark Tactician", tactician.getBlueprint().getTitle());
		assertNull(tactician.getBlueprint().getSubtitle());
		assertFalse(tactician.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, tactician.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, tactician.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, tactician.getBlueprint().getCardType());
		assertEquals(Race.MAN, tactician.getBlueprint().getRace());
		assertEquals(2, tactician.getBlueprint().getTwilightCost());
		assertEquals(6, tactician.getBlueprint().getStrength());
		assertEquals(3, tactician.getBlueprint().getVitality());
		assertEquals(5, tactician.getBlueprint().getResistance());
	}

	@Test
	public void AShadowPlayerReplacingTheFellowshipsCurrentSiteOffersTheArcheryChangeWithAnotherRohanManSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetReplacedSiteScenario();

		var tactician = scn.GetFreepsCard("tactician");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(tactician, guard);

		var nelya = scn.GetShadowCard("nelya");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var marksman3 = scn.GetShadowCard("marksman3");
		var shaft = scn.GetShadowSite("site2");
		var ruins = scn.GetShadowSite("site5");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		scn.MoveMinionsToTable(nelya, marksman1, marksman2, marksman3);
		scn.ShadowRefreshDecision();

		assertEquals(shaft, scn.GetCurrentSite());
		// No companion is an archer, and three minions are.
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(3, scn.GetShadowArcheryTotal());

		// The Shadow player's Nazgûl replaces Abandoned Mine Shaft, under the fellowship, with Sirannon Ruins.
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertEquals(ruins, scn.GetCurrentSite());
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(3, scn.GetShadowArcheryTotal());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tactician));
		scn.FreepsAcceptOptionalTrigger();
		// 0 + 2, and 3 - 2
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void PlayedWithRohanTokensInPlayItReinforcesOneOfThem() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tactician = scn.GetFreepsCard("tactician");
		var tactician2 = scn.GetFreepsCard("tactician2");
		var doom1 = scn.GetFreepsCard("doom1");
		var doom2 = scn.GetFreepsCard("doom2");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(tactician2);
		scn.MoveCardsToSupportArea(doom1, doom2, garrison);
		scn.AddTokensToCard(doom1, 1);
		scn.AddTokensToCard(doom2, 1);
		scn.AddTokensToCard(garrison, 1);
		scn.MoveCardsToHand(tactician);

		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(1, scn.GetCultureTokensOn(doom2));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		scn.StartGame();
		assertInZone(Zone.HAND, tactician);

		scn.FreepsPlayCard(tactician);
		// The copy played makes the offer; the copy already in play does not.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tactician));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tactician2));
		scn.FreepsAcceptOptionalTrigger();
		// Either [rohan] card's token; not the [gondor] token.
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom1, doom2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(garrison));
		scn.FreepsChooseCard(doom2);
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(doom2));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DeclinedNothingIsReinforcedAndAnotherRohanManPlayedAfterItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tactician = scn.GetFreepsCard("tactician");
		var guard = scn.GetFreepsCard("guard");
		var doom1 = scn.GetFreepsCard("doom1");
		scn.MoveCardsToSupportArea(doom1);
		scn.AddTokensToCard(doom1, 1);
		scn.MoveCardsToHand(tactician, guard);

		assertEquals(1, scn.GetCultureTokensOn(doom1));
		scn.StartGame();
		assertInZone(Zone.HAND, tactician, guard);

		scn.FreepsPlayCard(tactician);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tactician));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another [rohan] Man companion played with this one in play: no offer.
		scn.FreepsPlayCard(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tactician));
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoRohanTokenInPlayItsPlayOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tactician = scn.GetFreepsCard("tactician");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCardsToSupportArea(garrison);
		scn.AddTokensToCard(garrison, 1);
		scn.MoveCardsToHand(tactician);

		assertEquals(1, scn.GetCultureTokensOn(garrison));
		scn.StartGame();
		assertInZone(Zone.HAND, tactician);

		// Only a [gondor] token: nothing to reinforce, and nothing is asked.
		scn.FreepsPlayCard(tactician);
		assertInZone(Zone.FREE_CHARACTERS, tactician);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tactician));
		assertEquals(1, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheArcheryChangeLastsUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsPathScenario();

		var tactician = scn.GetFreepsCard("tactician");
		var guard = scn.GetFreepsCard("guard");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(tactician, guard);

		var nelya = scn.GetShadowCard("nelya");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var marksman3 = scn.GetShadowCard("marksman3");
		var shaft = scn.GetShadowSite("site2");
		var ruins = scn.GetShadowSite("site5");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		scn.MoveMinionsToTable(nelya, marksman1, marksman2, marksman3);
		scn.ShadowRefreshDecision();
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tactician));
		scn.FreepsAcceptOptionalTrigger();
		// 0 + 2, and 3 - 2
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());

		// The minions' one wound goes on Frodo; the fellowship's two kill two Marksmen.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(frodo);
		scn.ShadowChooseCard(marksman1);
		scn.ShadowChooseCard(marksman2);
		assertInZone(Zone.DISCARD, marksman1, marksman2);
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{guard, marksman3},
				new PhysicalCardImpl[]{tactician, nelya});
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(tactician);

		// In a skirmish that follows one already resolved: still in force.  One Marksman is left, and 1 - 2 reads 0.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());

		// At the regroup phase it is gone.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, marksman3);
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
	}

	@Test
	public void DeclinedOnTheFirstReplacementItIsOfferedAgainOnTheSecondWithARohanManAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsPathScenario();

		var tactician = scn.GetFreepsCard("tactician");
		var herdsman = scn.GetFreepsCard("herdsman");
		scn.MoveCompanionsToTable(tactician);
		scn.MoveAlliesToTable(herdsman);

		var nelya = scn.GetShadowCard("nelya");
		var marksman1 = scn.GetShadowCard("marksman1");
		var marksman2 = scn.GetShadowCard("marksman2");
		var marksman3 = scn.GetShadowCard("marksman3");
		var shaft = scn.GetShadowSite("site2");
		var ruins = scn.GetShadowSite("site5");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		scn.MoveMinionsToTable(nelya, marksman1, marksman2, marksman3);
		scn.ShadowRefreshDecision();
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(3, scn.GetShadowArcheryTotal());

		// The [rohan] Man ally is the other [rohan] Man spotted.  Declined: the totals stay.
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertEquals(ruins, scn.GetCurrentSite());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tactician));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(3, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The Nazgûl replaces the site again: offered again, and taken.
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(shaft);
		assertEquals(2, scn.GetWoundsOn(nelya));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(tactician));
		scn.FreepsAcceptOptionalTrigger();
		// 0 + 2, and 3 - 2
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOtherRohanManToSpotAReplacementOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsPathScenario();

		var tactician = scn.GetFreepsCard("tactician");
		var merry = scn.GetFreepsCard("merry");
		var faramir = scn.GetFreepsCard("faramir");
		scn.MoveCompanionsToTable(tactician, merry, faramir);

		var nelya = scn.GetShadowCard("nelya");
		var marksman1 = scn.GetShadowCard("marksman1");
		var shaft = scn.GetShadowSite("site2");
		var ruins = scn.GetShadowSite("site5");

		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		scn.MoveMinionsToTable(nelya, marksman1);
		scn.ShadowRefreshDecision();

		// The Tactician himself, a [rohan] Hobbit and a [gondor] Man: no other [rohan] Man.
		scn.ShadowUseCardAction(nelya);
		scn.ShadowChooseCardBPFromSelection(ruins);
		assertEquals(ruins, scn.GetCurrentSite());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tactician));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(1, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerReplacingTheCurrentSiteOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowsPathScenario();

		var tactician = scn.GetFreepsCard("tactician");
		var guard = scn.GetFreepsCard("guard");
		var way = scn.GetFreepsCard("way");
		var freepsRuins = scn.GetFreepsSite("site5");
		scn.MoveCompanionsToTable(tactician, guard);
		scn.MoveCardsToSupportArea(way);

		var shaft = scn.GetShadowSite("site2");

		assertInZone(Zone.SUPPORT, way);
		scn.StartGame(scn.GetFreepsSite("site1"));
		scn.FreepsPass();
		scn.ShadowChooseCardBPFromSelection(shaft);
		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(shaft, scn.GetCurrentSite());

		scn.FreepsUseCardAction(way);
		scn.FreepsChooseCardBPFromSelection(freepsRuins);
		assertEquals(freepsRuins, scn.GetCurrentSite());
		assertInZone(Zone.DISCARD, way);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tactician));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ReplacingASiteThatIsNotTheFellowshipsCurrentOneOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipPathScenario();

		var tactician = scn.GetFreepsCard("tactician");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(tactician, guard);

		var nelya = scn.GetShadowCard("nelya");
		var freepsSite1 = scn.GetFreepsSite(1);
		var shadowSite1 = scn.GetShadowSite(1);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		// The move lays the Shadow player's own site 2 under the fellowship: the Free Peoples player's site 1,
		// behind it, is the one opponent's site on the path.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertSame(freepsSite1, scn.GetSite(1));
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.ShadowUseCardAction(nelya);
		// A Shadow player replaces a site, but not the fellowship's current one.
		assertEquals(1, scn.GetWoundsOn(nelya));
		assertSame(shadowSite1, scn.GetSite(1));
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(tactician));
		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
