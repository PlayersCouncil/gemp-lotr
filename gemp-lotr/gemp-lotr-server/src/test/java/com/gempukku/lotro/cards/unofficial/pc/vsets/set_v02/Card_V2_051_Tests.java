package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wrath", "102_51");

					// Valiant companions of two cultures.
					put("guard", "5_83");        // Household Guard: [rohan] Man, strength 6, vitality 3; "Valiant" is his whole text
					put("haldir", "102_8");      // Haldir, Naith Commander: [elven] Elf; his own text needs an [elven] possession about to be discarded
					// Rohirrim Guard: a [rohan] companion who is not valiant, strength 6, vitality 3.
					// "Skirmish: Discard a card from hand to exert a minion skirmishing this companion."
					put("rguard", "7_246");
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man, for Athelas and The Red Arrow

					// Deor: a unique [rohan] ally, vitality 2.  His ability needs initiative and is never used.
					put("deor", "7_222");
					put("deor2", "7_222");
					put("bounder", "1_286");     // Bounder: a [shire] ally, vitality 2

					// [rohan] possessions, all "Bearer must be a [rohan] Man" (Heavy Chain: a Man).
					put("javelin", "7_248");     // Rohirrim Javelin: strength +1. "Archery: Discard this possession to make the fellowship archery total +1."
					put("bow", "5_88");          // Rohirrim Bow: "Archery: Exert bearer and discard this possession to wound a minion."
					put("shield", "5_91");       // Rohirrim Shield: minion archery total -1, and a regroup ability
					put("chain", "4_278");       // Heavy Chain: bearer is harder to overwhelm
					// Rider's Mount: "At the start of each skirmish involving bearer, each minion skirmishing bearer must exert."
					put("mount", "4_287");
					// The Red Arrow: the [rohan] artifact that needs no Theoden.  "Assignment: Exert a [gondor]
					// Man to play a [rohan] Man. ... Discard this artifact."
					put("arrow", "9_46");
					// The Red Arrow as its erratum has it, in the support area: "Fellowship: Discard this and
					// exert a Man to take a Free Peoples Man of a different culture into hand from your draw
					// deck."  The two share a unique title, so no test lays both.
					put("errataarrow", "59_46");
					// Athelas: a [gondor] possession.  "Fellowship: Discard this possession to heal a companion..."
					put("athelas1", "1_94");
					// Naith Longbow: "Archery: Exert bearer to discard the top card of your draw deck."
					put("longbow", "6_21");

					// Cast Out: a [rohan] condition.  "Maneuver: Spot a [rohan] Man and discard this condition
					// to exert a minion."  Its other ability needs a mounted Man.
					put("castout1", "12_108");
					put("castout2", "12_108");
					put("doom", "8_86");         // Doom Drove Them: another [rohan] condition that holds [rohan] tokens
					// Northern Signal-fire, Flame of Calenhad: a [rohan] possession in the support area.  "Maneuver:
					// Hinder this beacon and exert an unbound Man.  Until the regroup phase, each of your other Men
					// gain each of the unloaded keywords (except race) on that Man."
					put("flame", "103_86");
					// For the Mark: a [rohan] regroup event. "Spot a [rohan] Man to discard a possession from play."
					put("mark", "12_114");
					put("mark2", "12_114");
					put("villagers", "4_273");   // Fight for the Villagers: a [rohan] skirmish event

					put("cantea", "1_230");      // Ulaire Cantea: "Skirmish: Exert Ulaire Cantea to discard a weapon borne by a character he is skirmishing."
					put("terror", "1_226");      // Their Power Is in Terror: "Maneuver: Exert a Nazgul to wound every ally."
					put("fear1", "1_212");       // Fear: "Maneuver: Exert your Nazgul to discard an ally."
					put("fear2", "1_212");
					put("troop", "1_177");       // Goblin Patrol Troop: [moria], strength 13, vitality 3, no text
					put("savage", "1_151");      // Uruk Savage: [isengard], vitality 3
					put("hunter", "1_256");      // Morgul Hunter: vitality 4; stronger for each companion, nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NowforWrathStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Now for Wrath
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time your [rohan] item or [rohan] ally is killed or discarded from play or from hand
		 * (except during the fellowship phase), add a [rohan] token here.
		 * Each time a minion is exerted by a Free Peoples card, you may remove 2 [rohan] tokens here and exert a
		 * valiant companion to wound that minion.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wrath");

		assertEquals("Now for Wrath", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ARohanAllyKilledAddsATokenAndSoDoesTheRohanItemHeBore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var doom = scn.GetFreepsCard("doom");
		var deor = scn.GetFreepsCard("deor");
		var bounder = scn.GetFreepsCard("bounder");
		var javelin = scn.GetFreepsCard("javelin");
		var terror = scn.GetShadowCard("terror");
		var cantea = scn.GetShadowCard("cantea");
		scn.MoveCardsToSupportArea(wrath, doom);
		scn.AddTokensToCard(doom, 1);
		scn.MoveAlliesToTable(deor, bounder);
		scn.AttachCardsTo(deor, javelin);
		// Vitality 2 each: one more wound kills.
		scn.AddWoundsToChar(deor, 1);
		scn.AddWoundsToChar(bounder, 1);
		scn.MoveCardsToHand(terror);

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(wrath));
		assertInZone(Zone.SUPPORT, deor, bounder);
		assertAttachedTo(javelin, deor);

		scn.ShadowPlayCard(terror);
		// Both allies are dying at once: the Free Peoples player orders the deaths and the tokens.
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.DEAD, deor, bounder);
		assertInZone(Zone.DISCARD, javelin);
		// One for Deor killed and one for the Javelin discarded with him; none for the [shire] ally.
		assertEquals(2, scn.GetCultureTokensOn(wrath));
		// "Here": the other [rohan] condition that holds tokens gets none.
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ARohanAllyDiscardedFromPlayAddsATokenAndAShireAllyOrARohanConditionAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var castout = scn.GetFreepsCard("castout1");
		var guard = scn.GetFreepsCard("guard");
		var deor = scn.GetFreepsCard("deor");
		var bounder = scn.GetFreepsCard("bounder");
		var fear1 = scn.GetShadowCard("fear1");
		var fear2 = scn.GetShadowCard("fear2");
		var cantea = scn.GetShadowCard("cantea");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(wrath, castout);
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(deor, bounder);
		scn.MoveCardsToHand(fear1, fear2);

		scn.StartGame();
		scn.MoveMinionsToTable(cantea, troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetCultureTokensOn(wrath));

		// Cast Out discards itself as its cost: a [rohan] condition, neither an item nor an ally.
		scn.FreepsUseCardAction(castout);
		scn.FreepsChooseCard(troop);
		assertInZone(Zone.DISCARD, castout);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetCultureTokensOn(wrath));

		// Fear discards an ally: first the [shire] one.
		scn.ShadowPlayCard(fear1);
		assertTrue(scn.ShadowHasCardChoiceAvailable(deor, bounder));
		scn.ShadowChooseCard(bounder);
		assertInZone(Zone.DISCARD, bounder);
		assertEquals(0, scn.GetCultureTokensOn(wrath));

		// Then the [rohan] one.
		scn.FreepsPass();
		scn.ShadowPlayCard(fear2);
		assertInZone(Zone.DISCARD, deor);
		assertEquals(1, scn.GetCultureTokensOn(wrath));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ARohanPossessionOrArtifactDiscardedFromPlayAddsATokenAndAGondorPossessionAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var doom = scn.GetFreepsCard("doom");
		var arrow = scn.GetFreepsCard("arrow");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rguard = scn.GetFreepsCard("rguard");
		var javelin = scn.GetFreepsCard("javelin");
		var chain = scn.GetFreepsCard("chain");
		var athelas = scn.GetFreepsCard("athelas1");
		var mark = scn.GetFreepsCard("mark");
		var mark2 = scn.GetFreepsCard("mark2");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(wrath, doom, arrow);
		scn.AddTokensToCard(doom, 1);
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.AttachCardsTo(guard, javelin, chain);
		scn.AttachCardsTo(aragorn, athelas);
		scn.MoveCardsToHand(rguard, mark, mark2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetCultureTokensOn(wrath));

		// A [rohan] possession, discarded by its own ability.
		scn.FreepsUseCardAction(javelin);
		assertInZone(Zone.DISCARD, javelin);
		assertEquals(1, scn.GetCultureTokensOn(wrath));

		// A [rohan] artifact: The Red Arrow has Aragorn exert to play a [rohan] Man, and is then discarded.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsUseCardAction(arrow);
		// "You may exert that [rohan] Man to play a possession on him or her": no possession is in hand, so nothing is asked (F-0472).
		assertInZone(Zone.FREE_CHARACTERS, rguard);
		assertInZone(Zone.DISCARD, arrow);
		assertEquals(2, scn.GetCultureTokensOn(wrath));

		scn.ShadowPass();
		scn.FreepsPass();
		scn.FreepsDeclineAssignments();
		scn.ShadowDeclineAssignments();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// For the Mark discards a possession from play: the [gondor] one adds nothing.
		scn.FreepsPlayCard(mark);
		assertTrue(scn.FreepsHasCardChoiceAvailable(athelas, chain));
		scn.FreepsChooseCard(athelas);
		assertInZone(Zone.DISCARD, athelas);
		assertEquals(2, scn.GetCultureTokensOn(wrath));

		// And the [rohan] one, the only possession left, adds one.
		scn.ShadowPass();
		scn.FreepsPlayCard(mark2);
		assertInZone(Zone.DISCARD, chain);
		assertEquals(3, scn.GetCultureTokensOn(wrath));
		assertEquals(1, scn.GetCultureTokensOn(doom));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ARohanCompanionKilledAddsNoTokenButEachRohanItemHeBoreAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var rguard = scn.GetFreepsCard("rguard");
		var javelin = scn.GetFreepsCard("javelin");
		var shield = scn.GetFreepsCard("shield");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(wrath);
		scn.MoveCompanionsToTable(rguard);
		scn.AttachCardsTo(rguard, javelin, shield);
		// Vitality 3 with 2 wounds: losing the skirmish kills him.
		scn.AddWoundsToChar(rguard, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(rguard, troop);

		assertEquals(0, scn.GetCultureTokensOn(wrath));
		// 6 + 1 for the Javelin against 13: he loses, and is not overwhelmed.
		assertEquals(7, scn.GetStrength(rguard));
		scn.PassSkirmishActions();
		// The rest of the skirmish's resolution and the two tokens come at once, and are put in order.
		scn.FreepsResolveRuleFirst();

		assertInZone(Zone.DEAD, rguard);
		assertInZone(Zone.DISCARD, javelin, shield);
		// One for each of the two possessions, none for the companion.
		assertEquals(2, scn.GetCultureTokensOn(wrath));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachRohanItemOrAllyDiscardedFromHandAddsATokenAndOtherCardsAddNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var rguard = scn.GetFreepsCard("rguard");
		var villagers = scn.GetFreepsCard("villagers");
		var guard = scn.GetFreepsCard("guard");
		var castout = scn.GetFreepsCard("castout1");
		var athelas = scn.GetFreepsCard("athelas1");
		var bounder = scn.GetFreepsCard("bounder");
		var javelin = scn.GetFreepsCard("javelin");
		var arrow = scn.GetFreepsCard("arrow");
		var deor = scn.GetFreepsCard("deor");
		var mark = scn.GetFreepsCard("mark");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(wrath);
		// The Rohirrim Guard discards a card from hand each time his ability is used.  He is not
		// valiant, and nobody else is in play, so the minion he exerts brings no offer.
		scn.MoveCompanionsToTable(rguard);
		// For the Mark stays in hand throughout, so that every discard is a choice.
		scn.MoveCardsToHand(villagers, guard, castout, athelas, bounder, javelin, arrow, deor, mark);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(rguard, troop);

		assertEquals(0, scn.GetCultureTokensOn(wrath));

		// [rohan] cards that are neither items nor allies: an event, a companion, a condition.
		scn.FreepsUseCardAction(rguard);
		scn.FreepsChooseCard(villagers);
		assertInZone(Zone.DISCARD, villagers);
		assertEquals(0, scn.GetCultureTokensOn(wrath));
		scn.ShadowPass();

		scn.FreepsUseCardAction(rguard);
		scn.FreepsChooseCard(guard);
		assertInZone(Zone.DISCARD, guard);
		assertEquals(0, scn.GetCultureTokensOn(wrath));
		scn.ShadowPass();

		scn.FreepsUseCardAction(rguard);
		scn.FreepsChooseCard(castout);
		assertInZone(Zone.DISCARD, castout);
		assertEquals(0, scn.GetCultureTokensOn(wrath));
		scn.ShadowPass();

		// An item and an ally of other cultures.
		scn.FreepsUseCardAction(rguard);
		scn.FreepsChooseCard(athelas);
		assertInZone(Zone.DISCARD, athelas);
		assertEquals(0, scn.GetCultureTokensOn(wrath));
		scn.ShadowPass();

		scn.FreepsUseCardAction(rguard);
		scn.FreepsChooseCard(bounder);
		assertInZone(Zone.DISCARD, bounder);
		assertEquals(0, scn.GetCultureTokensOn(wrath));
		scn.ShadowPass();

		// A [rohan] possession, a [rohan] artifact and a [rohan] ally.
		scn.FreepsUseCardAction(rguard);
		scn.FreepsChooseCard(javelin);
		assertInZone(Zone.DISCARD, javelin);
		assertEquals(1, scn.GetCultureTokensOn(wrath));
		scn.ShadowPass();

		scn.FreepsUseCardAction(rguard);
		scn.FreepsChooseCard(arrow);
		assertInZone(Zone.DISCARD, arrow);
		assertEquals(2, scn.GetCultureTokensOn(wrath));
		scn.ShadowPass();

		scn.FreepsUseCardAction(rguard);
		scn.FreepsChooseCard(deor);
		assertInZone(Zone.DISCARD, deor);
		assertEquals(3, scn.GetCultureTokensOn(wrath));

		assertInZone(Zone.HAND, mark);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ARohanItemDiscardedFromHandWhenReconcilingAddsAToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var javelin = scn.GetFreepsCard("javelin");
		scn.MoveCardsToSupportArea(wrath);
		scn.MoveCardsToHand(javelin);

		scn.StartGame();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToStay();

		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetCultureTokensOn(wrath));
		assertTrue(scn.FreepsDecisionAvailable("reconcile"));

		scn.FreepsChooseCard(javelin);
		assertInZone(Zone.DISCARD, javelin);
		assertEquals(1, scn.GetCultureTokensOn(wrath));
		// The turn is over; the other player's has begun.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void TheOtherPlayersRohanAllyDiscardedFromHisHandAddsNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var deor = scn.GetFreepsCard("deor");
		// The Shadow player's own copy of Deor, in his hand.
		var theirDeor = scn.GetShadowCard("deor");
		scn.MoveCardsToSupportArea(wrath);
		scn.MoveCardsToHand(deor, theirDeor);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassRegroupActions();

		// The Shadow player reconciles before the Free Peoples player decides whether to move again.
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertTrue(scn.ShadowDecisionAvailable("reconcile"));
		assertEquals(0, scn.GetCultureTokensOn(wrath));

		// "Your [rohan] item or [rohan] ally": the other player's own Deor is not this card's owner's.
		scn.ShadowChooseCard(theirDeor);
		assertInZone(Zone.DISCARD, theirDeor);
		assertEquals(0, scn.GetCultureTokensOn(wrath));

		// The same card discarded the same way from the owner's own hand adds one.
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		scn.FreepsChooseToStay();
		assertTrue(scn.FreepsDecisionAvailable("reconcile"));
		scn.FreepsChooseCard(deor);
		assertInZone(Zone.DISCARD, deor);
		assertEquals(1, scn.GetCultureTokensOn(wrath));
		// The turn is over; the other player's has begun.
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void NothingIsAddedDuringTheFellowshipPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var deor = scn.GetFreepsCard("deor");
		var deor2 = scn.GetFreepsCard("deor2");
		var arrow = scn.GetFreepsCard("errataarrow");
		var chain = scn.GetFreepsCard("chain");
		var mark = scn.GetFreepsCard("mark");
		scn.MoveCardsToSupportArea(wrath, arrow);
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(deor);
		scn.AddWoundsToChar(deor, 1);
		scn.MoveCardsToHand(deor2, mark);
		// The Guard bears a [rohan] possession for the regroup phase.
		scn.AttachCardsTo(guard, chain);

		scn.StartGame();

		assertEquals(Phase.FELLOWSHIP, scn.GetCurrentPhase());
		assertEquals(0, scn.GetCultureTokensOn(wrath));

		// A [rohan] ally discarded from hand, to heal his copy in play.
		scn.FreepsDiscardToHeal(deor2);
		assertInZone(Zone.DISCARD, deor2);
		assertEquals(0, scn.GetWoundsOn(deor));
		assertEquals(0, scn.GetCultureTokensOn(wrath));

		// A [rohan] item discarded from play: The Red Arrow, a [rohan] artifact, discards itself as the cost of
		// its own fellowship ability.  The Guard exerts for it, and Aragorn, a Man of [gondor], comes from
		// the draw deck.
		scn.FreepsUseCardAction(arrow);
		scn.FreepsChooseCard(guard);
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(aragorn);
		assertInZone(Zone.DISCARD, arrow);
		assertEquals(1, scn.GetWoundsOn(guard));
		assertInZone(Zone.HAND, aragorn);
		assertEquals(0, scn.GetCultureTokensOn(wrath));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [rohan] possession discarded by For the Mark in the regroup phase of the same turn adds a token.
		scn.SkipToPhase(Phase.REGROUP);
		scn.FreepsPlayCard(mark);
		assertInZone(Zone.DISCARD, chain);
		assertEquals(1, scn.GetCultureTokensOn(wrath));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ARohanItemDiscardedFromTheDrawDeckAddsNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var haldir = scn.GetFreepsCard("haldir");
		var longbow = scn.GetFreepsCard("longbow");
		var javelin = scn.GetFreepsCard("javelin");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(wrath);
		scn.MoveCompanionsToTable(haldir);
		scn.AttachCardsTo(haldir, longbow);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.MoveCardsToTopOfDeck(javelin);

		assertEquals(javelin, scn.GetFreepsTopOfDeck());
		assertEquals(0, scn.GetCultureTokensOn(wrath));

		// The Naith Longbow discards the top card of the draw deck: neither from play nor from hand.
		scn.FreepsUseCardAction(longbow);
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertInZone(Zone.DISCARD, javelin);
		assertEquals(0, scn.GetCultureTokensOn(wrath));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void RemovesTwoTokensAndExertsAValiantCompanionToWoundTheMinionAFreePeoplesCardExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var wrath = scn.GetFreepsCard("wrath");
		var doom = scn.GetFreepsCard("doom");
		var castout = scn.GetFreepsCard("castout1");
		var guard = scn.GetFreepsCard("guard");
		var haldir = scn.GetFreepsCard("haldir");
		var rguard = scn.GetFreepsCard("rguard");
		var deor = scn.GetFreepsCard("deor");
		var flame = scn.GetFreepsCard("flame");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(wrath, doom, castout, flame);
		scn.AddTokensToCard(wrath, 3);
		scn.AddTokensToCard(doom, 2);
		scn.MoveCompanionsToTable(guard, haldir, rguard);
		scn.MoveAlliesToTable(deor);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// No ally is valiant in print.  The Flame of Calenhad has the Household Guard exert, and until the
		// regroup phase the player's other Men have his keyword: the Rohirrim Guard, a companion, and Deor,
		// an ally.
		assertFalse(scn.HasKeyword(rguard, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(deor, Keyword.VALIANT));
		scn.FreepsUseCardAction(flame);
		scn.FreepsChooseCard(guard);
		assertTrue(scn.HasKeyword(rguard, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(deor, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));
		scn.ShadowPass();

		assertEquals(3, scn.GetCultureTokensOn(wrath));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));

		// Cast Out, a Free Peoples condition, exerts a minion: a [moria] Orc or an [isengard] Uruk-hai.
		scn.FreepsUseCardAction(castout);
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		scn.FreepsChooseCard(savage);
		assertEquals(1, scn.GetWoundsOn(savage));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wrath));
		scn.FreepsAcceptOptionalTrigger();
		// The valiant companions, of two cultures, in print or by the Flame.  Not the ally who is valiant by
		// the Flame as the Rohirrim Guard is, nor Frodo, who is not valiant.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, haldir, rguard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(deor, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(haldir);

		// 2 of the 3 tokens here are removed, and none of the other condition's.
		assertEquals(1, scn.GetCultureTokensOn(wrath));
		assertEquals(2, scn.GetCultureTokensOn(doom));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(rguard));
		// The exertion and the wound, on the minion that was exerted and not the other.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void MayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var castout = scn.GetFreepsCard("castout1");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(wrath, castout);
		scn.AddTokensToCard(wrath, 2);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsUseCardAction(castout);
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wrath));
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetCultureTokensOn(wrath));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsOfferedAgainEachTimeTheSameMinionIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var castout1 = scn.GetFreepsCard("castout1");
		var castout2 = scn.GetFreepsCard("castout2");
		var guard = scn.GetFreepsCard("guard");
		var hunter = scn.GetShadowCard("hunter");
		scn.MoveCardsToSupportArea(wrath, castout1, castout2);
		scn.AddTokensToCard(wrath, 4);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(hunter);
		scn.SkipToPhase(Phase.MANEUVER);

		// The only minion is exerted, and the only valiant companion pays.
		scn.FreepsUseCardAction(castout1);
		assertEquals(1, scn.GetWoundsOn(hunter));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wrath));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(wrath));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetWoundsOn(hunter));

		scn.ShadowPass();
		scn.FreepsUseCardAction(castout2);
		assertEquals(3, scn.GetWoundsOn(hunter));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wrath));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(wrath));
		assertEquals(2, scn.GetWoundsOn(guard));
		// Vitality 4: two exertions and two wounds kill the Morgul Hunter.
		assertInZone(Zone.DISCARD, hunter);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void EachOfTwoMinionsAMountExertsAtOnceBringsAnOfferOfItsOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var guard = scn.GetFreepsCard("guard");
		var haldir = scn.GetFreepsCard("haldir");
		var mount = scn.GetFreepsCard("mount");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(wrath);
		scn.AddTokensToCard(wrath, 4);
		scn.MoveCompanionsToTable(guard, haldir);
		scn.AttachCardsTo(guard, mount);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToAssignments();
		// Two minions on the mounted Guard: one assigned by each player.
		scn.FreepsAssignToMinions(guard, troop);
		scn.ShadowAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);

		// The skirmish starts and the Mount, a Free Peoples possession, has made both minions exert.
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(4, scn.GetCultureTokensOn(wrath));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wrath));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, haldir));
		scn.FreepsChooseCard(haldir);
		assertEquals(2, scn.GetCultureTokensOn(wrath));
		// One of the two minions has its wound; which is offered first is the game's to say.
		assertEquals(3, scn.GetWoundsOn(troop) + scn.GetWoundsOn(savage));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(wrath));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(guard);
		assertEquals(0, scn.GetCultureTokensOn(wrath));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(guard));
		// Each offer wounded the minion it was made for.
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(2, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWithOneTokenHereThoughAnotherRohanConditionHasTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var doom = scn.GetFreepsCard("doom");
		var castout = scn.GetFreepsCard("castout1");
		var guard = scn.GetFreepsCard("guard");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(wrath, doom, castout);
		scn.AddTokensToCard(wrath, 1);
		scn.AddTokensToCard(doom, 2);
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(guard));
		scn.FreepsUseCardAction(castout);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetCultureTokensOn(wrath));
		assertEquals(2, scn.GetCultureTokensOn(doom));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenTheOnlyValiantCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var wrath = scn.GetFreepsCard("wrath");
		var castout = scn.GetFreepsCard("castout1");
		var guard = scn.GetFreepsCard("guard");
		var deor = scn.GetFreepsCard("deor");
		var flame = scn.GetFreepsCard("flame");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(wrath, castout, flame);
		scn.AddTokensToCard(wrath, 2);
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(deor);
		scn.AddWoundsToChar(guard, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Flame of Calenhad has the Household Guard, the only unbound Man, exert: vitality 3 with 2
		// wounds, the valiant companion cannot exert again.  Until the regroup phase Deor, the player's
		// other Man, has his keyword: a valiant ally, who can exert, as Frodo can, who is not valiant.
		scn.FreepsUseCardAction(flame);
		assertTrue(scn.IsExhausted(guard));
		assertTrue(scn.HasKeyword(deor, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));
		scn.ShadowPass();

		scn.FreepsUseCardAction(castout);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(2, scn.GetCultureTokensOn(wrath));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(deor));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAFreePeoplesCardWoundsAMinionWithoutExertingIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var guard = scn.GetFreepsCard("guard");
		var haldir = scn.GetFreepsCard("haldir");
		var bow = scn.GetFreepsCard("bow");
		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(wrath);
		scn.AddTokensToCard(wrath, 2);
		scn.MoveCompanionsToTable(guard, haldir);
		scn.AttachCardsTo(guard, bow);

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage);
		scn.SkipToPhase(Phase.ARCHERY);

		// The Rohirrim Bow exerts its bearer and is discarded to wound a minion.
		scn.FreepsUseCardAction(bow);
		scn.FreepsChooseCard(troop);

		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		// The Bow, a [rohan] item discarded from play, has added a token; none was spent.
		assertInZone(Zone.DISCARD, bow);
		assertEquals(3, scn.GetCultureTokensOn(wrath));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAShadowCardOrASiteExertsAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var wrath = scn.GetFreepsCard("wrath");
		var guard = scn.GetFreepsCard("guard");
		var haldir = scn.GetFreepsCard("haldir");
		var javelin = scn.GetFreepsCard("javelin");
		var cantea = scn.GetShadowCard("cantea");
		scn.MoveCardsToSupportArea(wrath);
		scn.AddTokensToCard(wrath, 2);
		scn.MoveCompanionsToTable(guard, haldir);
		scn.AttachCardsTo(guard, javelin);

		scn.StartGame();
		scn.MoveMinionsToTable(cantea);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, cantea);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(cantea));
		assertAttachedTo(javelin, guard);

		// Ulaire Cantea exerts himself to discard the Javelin.
		scn.ShadowUseCardAction(cantea);

		assertEquals(1, scn.GetWoundsOn(cantea));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		// The Javelin, discarded from play by a Shadow card, has added a token; none was spent.
		assertInZone(Zone.DISCARD, javelin);
		assertEquals(3, scn.GetCultureTokensOn(wrath));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Site 2, Ettenmoors: "Skirmish: Exert your companion or minion to make that character strength +2."
		// A site is neither a Free Peoples card nor a Shadow card.  Cantea is the Shadow player's only minion.
		scn.FreepsPass();
		var ettenmoors = scn.GetCurrentSite();
		assertTrue(scn.ShadowActionAvailable(ettenmoors));
		scn.ShadowUseCardAction(ettenmoors);

		assertEquals(2, scn.GetWoundsOn(cantea));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetCultureTokensOn(wrath));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
