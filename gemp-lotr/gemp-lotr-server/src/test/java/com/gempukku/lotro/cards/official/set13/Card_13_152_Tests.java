package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_152_Tests
{

	// The Fellowship path with Derndingle 4_330 (a forest) as site 2 and Rivendell Waterfall 1_342 (a forest and a
	// sanctuary) as site 3; site 1 Bag End has no terrain and site 4 Balin's Tomb is underground.  Two Shadows-block
	// dwellings stand by, laid after the current site by MoveCardToAdventurePath.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_152");

					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion (cheated in); a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit (cheated in); an assignment ability only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, twilight 1; a skirmish ability used only on purpose
					put("follow", "1_109");     // One Whom Men Would Follow: [gondor] maneuver event, exert Aragorn so that an ally takes part in skirmishes

					// Kept Safe: another [shire] support-area condition, "When you play this condition, place 2 [shire]
					// tokens here"; a response to a wound on an unbound Hobbit (nobody is wounded where it is laid)
					put("keptsafe", "6_111");
					// Sudden Fury: [shire] support-area condition holding [shire] tokens; a token for each [shire]
					// companion who loses a skirmish (nobody does); a skirmish ability used only on purpose
					put("fury", "12_132");

					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion, strength 13, no text
					put("troop2", "1_177");
					put("troop3", "1_177");
					put("ted", "18_75");        // Ted Sandyman, Chief's Men's Ally: a [men] minion who is a Hobbit; abilities used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site2", "4_330");      // Derndingle: Forest; a move-limit text for an unbound Hobbit there
					put("site3", "1_342");      // Rivendell Waterfall: Forest, Sanctuary; a move-limit text for a ranger there
					// Streets of Bree: a dwelling and nothing else; a bar on transferring followers.
					put("bree", "18_140");
					// Valley of the Silverlode: a dwelling and a river; "When your fellowship moves to this site, you may
					// heal each Hobbit companion" (it is laid ahead of the fellowship and never moved to).
					put("valley", "11_261");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path as it comes: Bag End, Ettenmoors (plains), Rivendell Valley (sanctuary), Balin's Tomb
	// (underground); Streets of Bree 18_140, a dwelling and nothing else, is laid ahead by MoveCardToAdventurePath.
	protected VirtualTableScenario GetPlainPathScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_152");

					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion (cheated in); a skirmish ability used only on purpose
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("bree", "18_140");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship block format, where a skirmish involving the Ring-bearer may be cancelled.
	protected VirtualTableScenario GetFellowshipBlockScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("homestead", "13_152");
					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc minion, strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				VirtualTableScenario.Fellowship
		);
	}

	@Test
	public void HumbleHomesteadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Humble Homestead
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot 2 Hobbits.<br>When you play this, add a [shire] token here for each forest site and each dwelling site on the adventure path.<br><b>Skirmish:</b> Discard this from play or remove 3 tokens from here to cancel a skirmish involving a Hobbit.
		*/

		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");

		assertEquals("Humble Homestead", homestead.getBlueprint().getTitle());
		assertNull(homestead.getBlueprint().getSubtitle());
		assertTrue(homestead.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, homestead.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, homestead.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, homestead.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(homestead, Keyword.SUPPORT_AREA));
		assertEquals(2, homestead.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilASecondHobbitIsSpottedAndAHobbitAllyIsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(smeagol, aragorn);

		scn.StartGame();
		scn.MoveCardsToHand(homestead, bounder);
		scn.FreepsRefreshDecision();

		// Frodo, the Ring-bearer, is the one Hobbit: Smeagol is [shire] and no Hobbit, Aragorn a Man.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(homestead));

		// A second Hobbit, an ally, is played.
		scn.FreepsPlayCard(bounder);
		assertInZone(Zone.SUPPORT, bounder);
		assertTrue(scn.FreepsPlayAvailable(homestead));
		scn.FreepsPlayCard(homestead);

		assertInZone(Zone.SUPPORT, homestead);
		// At Bag End, site 1, with no terrain: no token.
		assertEquals(0, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSiteFourItTakesATokenForEachForestAndTheDwellingLaidAhead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var pippin = scn.GetFreepsCard("pippin");
		var keptsafe = scn.GetFreepsCard("keptsafe");
		var bree = scn.GetShadowSite("bree");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(keptsafe);
		scn.AddTokensToCard(keptsafe, 2);

		scn.StartGame();
		assertEquals(2, scn.GetCultureTokensOn(keptsafe));
		scn.SkipToSite(4);
		scn.MoveCardToAdventurePath(bree);
		scn.MoveCardsToHand(homestead);
		scn.FreepsRefreshDecision();

		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetSite(3), Keyword.FOREST));
		assertTrue(scn.HasKeyword(scn.GetSite(3), Keyword.SANCTUARY));
		assertTrue(scn.HasKeyword(scn.GetSite(4), Keyword.UNDERGROUND));
		assertSame(bree, scn.GetSite(5));
		assertTrue(scn.HasKeyword(bree, Keyword.DWELLING));

		// Frodo, a [shire] Hobbit, and Pippin, a [gondor] one: two Hobbits.
		assertTrue(scn.FreepsPlayAvailable(homestead));
		scn.FreepsPlayCard(homestead);

		assertInZone(Zone.SUPPORT, homestead);
		// Derndingle, Rivendell Waterfall and Streets of Bree; not Bag End or Balin's Tomb.
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		assertEquals(2, scn.GetCultureTokensOn(keptsafe));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AtSiteTwoItTakesATokenForTheForestAndForADwellingThatIsARiverAndNoneForAnotherCardPlayedAfterIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var pippin = scn.GetFreepsCard("pippin");
		var keptsafe = scn.GetFreepsCard("keptsafe");
		var valley = scn.GetShadowSite("valley");
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardToAdventurePath(valley);
		scn.MoveCardsToHand(homestead, keptsafe);
		scn.FreepsRefreshDecision();

		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.FOREST));
		// The Valley stands where Rivendell Waterfall would: the Waterfall is not on the path.
		assertSame(valley, scn.GetSite(3));
		assertTrue(scn.HasKeyword(valley, Keyword.DWELLING));
		assertTrue(scn.HasKeyword(valley, Keyword.RIVER));
		assertNull(scn.GetSite(4));

		scn.FreepsPlayCard(homestead);
		// Derndingle and the Valley.
		assertEquals(2, scn.GetCultureTokensOn(homestead));

		// Another [shire] condition played after it puts its own tokens on itself, and none here.
		scn.FreepsPlayCard(keptsafe);
		assertInZone(Zone.SUPPORT, keptsafe);
		assertEquals(2, scn.GetCultureTokensOn(keptsafe));
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SitesOfOtherTerrainOnThePathAddNoTokenBesideTheDwellingLaidAhead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetPlainPathScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var pippin = scn.GetFreepsCard("pippin");
		var bree = scn.GetShadowSite("bree");
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardToAdventurePath(bree);
		scn.MoveCardsToHand(homestead);
		scn.FreepsRefreshDecision();

		assertTrue(scn.HasKeyword(scn.GetSite(2), Keyword.PLAINS));
		assertTrue(scn.HasKeyword(scn.GetSite(3), Keyword.SANCTUARY));
		assertTrue(scn.HasKeyword(scn.GetSite(4), Keyword.UNDERGROUND));
		assertSame(bree, scn.GetSite(5));
		assertTrue(scn.HasKeyword(bree, Keyword.DWELLING));

		scn.FreepsPlayCard(homestead);
		// Streets of Bree alone: not a plains, a sanctuary or an underground site.
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AControlledForestSiteIsNoLongerOnThePathAndAddsNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var pippin = scn.GetFreepsCard("pippin");
		var bree = scn.GetShadowSite("bree");
		scn.MoveCompanionsToTable(pippin);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.MoveCardToAdventurePath(bree);
		var bagEnd = scn.GetSite(1);
		var derndingle = scn.GetSite(2);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveCardsToHand(homestead);
		scn.FreepsRefreshDecision();

		// Derndingle is still a forest, but a controlled site is in its controller's support area.
		assertTrue(scn.IsSiteControlled(bagEnd));
		assertTrue(scn.IsSiteControlled(derndingle));
		assertTrue(scn.HasKeyword(derndingle, Keyword.FOREST));
		assertInZone(Zone.SUPPORT, derndingle);

		scn.FreepsPlayCard(homestead);
		// Rivendell Waterfall and Streets of Bree; not the controlled Derndingle.
		assertEquals(2, scn.GetCultureTokensOn(homestead));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesThreeTokensOrDiscardsItToCancelASkirmishInvolvingAHobbitOfAnyCultureTypeOrSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var fury = scn.GetFreepsCard("fury");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var follow = scn.GetFreepsCard("follow");
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var ted = scn.GetShadowCard("ted");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(homestead, fury);
		// Seven forest and dwelling sites on the path would have given it seven.
		scn.AddTokensToCard(homestead, 7);
		scn.AddTokensToCard(fury, 2);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, homestead, fury);
		assertEquals(7, scn.GetCultureTokensOn(homestead));
		assertEquals(2, scn.GetCultureTokensOn(fury));
		scn.MoveCardsToHand(follow);
		scn.MoveMinionsToTable(troop1, troop2, ted);
		scn.SkipToPhase(Phase.MANEUVER);
		// An ally takes part in skirmishes only by a card's text: One Whom Men Would Follow makes Bounder take part.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, troop1}, new PhysicalCardImpl[]{aragorn, ted},
				new PhysicalCardImpl[]{bounder, troop2});

		// A [gondor] Hobbit companion's skirmish.  Both costs can be paid: the player is asked which.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, troop1));
		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);
		assertEquals(List.of("Discard this from play", "Remove 3 tokens from here"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Remove 3 tokens from here");
		assertEquals(4, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, homestead);
		assertFalse(scn.IsCharSkirmishing(pippin));
		assertFalse(scn.IsCharSkirmishing(troop1));

		// The Shadow player's Hobbit, a minion, skirmishing a Man.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, ted));
		scn.FreepsUseCardAction(homestead);
		scn.FreepsChoose("Remove 3 tokens from here");
		assertEquals(1, scn.GetCultureTokensOn(homestead));
		assertFalse(scn.IsCharSkirmishing(aragorn));
		assertFalse(scn.IsCharSkirmishing(ted));

		// A Hobbit ally's.  With one token left only the discard can be paid, and it is paid unasked; Sudden Fury's
		// tokens are not "from here".
		scn.FreepsResolveSkirmish(bounder);
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, troop2));
		scn.FreepsUseCardAction(homestead);
		assertInDiscard(homestead);
		assertEquals(2, scn.GetCultureTokensOn(fury));
		assertFalse(scn.IsCharSkirmishing(bounder));
		assertFalse(scn.IsCharSkirmishing(troop2));

		// Nobody fought: Aragorn's one wound is his exertion.
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(bounder));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InASkirmishInvolvingNoHobbitItIsUsedAndNothingIsCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var smeagol = scn.GetFreepsCard("smeagol");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 3);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, homestead);
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, troop1);
		scn.FreepsResolveSkirmish(smeagol);

		// Smeagol is [shire] and no Hobbit: the ability is offered, its cost paid, and nothing is cancelled.
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, troop1));
		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);
		scn.FreepsChoose("Remove 3 tokens from here");
		assertEquals(0, scn.GetCultureTokensOn(homestead));
		assertInZone(Zone.SUPPORT, homestead);
		assertTrue(scn.IsCharSkirmishingAgainst(smeagol, troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var pippin = scn.GetFreepsCard("pippin");
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCompanionsToTable(pippin);
		scn.MoveCardsToSupportArea(homestead);
		scn.AddTokensToCard(homestead, 3);

		scn.StartGame();
		assertEquals(3, scn.GetCultureTokensOn(homestead));
		scn.MoveMinionsToTable(troop1);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(homestead));

		// In the skirmish that follows, it is.
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(pippin, troop1);
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(homestead));
		assertEquals(3, scn.GetCultureTokensOn(homestead));
	}

	@Test
	public void InTheFellowshipBlockFormatItCancelsTheRingBearersSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFellowshipBlockScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var frodo = scn.GetRingBearer();
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCardsToSupportArea(homestead);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, homestead);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		// With no token on it, the discard is the cost, paid unasked.
		scn.FreepsUseCardAction(homestead);

		assertInDiscard(homestead);
		assertFalse(scn.IsCharSkirmishing(frodo));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InMultipathTheRingBearersSkirmishIsNotCancelled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var homestead = scn.GetFreepsCard("homestead");
		var frodo = scn.GetRingBearer();
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveCardsToSupportArea(homestead);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, homestead);
		scn.MoveMinionsToTable(troop1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, troop1);
		scn.FreepsResolveSkirmish(frodo);

		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertTrue(scn.FreepsActionAvailable(homestead));
		scn.FreepsUseCardAction(homestead);

		assertInDiscard(homestead);
		assertTrue(scn.IsCharSkirmishingAgainst(frodo, troop1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
