package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_045_Tests
{

	// The Hobbit table: the Hobbit path, no card in the Ring slot, and this Bilbo as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4; a regroup ability only
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: [wraith] minion, strength 5; an assignment ability that wants threats or a companion of cost 0 who is not the Ring-bearer
					put("shelob", "8_26");          // Shelob, Last Child of Ungoliant: [gollum] minion who is no Gollum, strength 8, fierce
					put("smaug", "30_66");          // Smaug, The Golden: [smaug] minion, strength 17, fierce; discarded if not at a mountain
					put("runner", "1_178");         // Goblin Runner: [moria] Orc, strength 5, vitality 1; a text on being played, never reached
					put("shaman", "1_152");         // Uruk Shaman: [isengard] Uruk-hai, "Maneuver: Remove (2) to heal an Uruk-hai"
					put("balin", "30_2");           // Balin, Brother of Dwalin: [dwarven] Dwarf companion, strength 7, vitality 3; a fellowship ability only
					put("oin", "30_16");            // Óin, Knowledgeable Healer: [dwarven] follower, Aid - Discard a [dwarven] card from hand; "When you transfer Óin, heal bearer"
					put("battlefury", "30_3");      // Battle Fury: a [dwarven] skirmish event, here only a [dwarven] card in hand for Óin's Aid
					put("elrond", "30_21");         // Elrond, Elven Lord: [elven] ally, home 3H; "Regroup: Exert Elrond twice to heal a companion"; a start-of-turn heal that has nobody to heal
					put("lindir", "33_14");         // Lindir, Elven Minstrel: [elven] ally, home 3H; "Each time the fellowship moves, you may heal an [elven] ally"
				}},
				null,
				"30_45",
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BilboStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Bilbo, Master in Riddles
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 8
		 * Can be the Ring-bearer: True
		 * Game Text: <b>Burglar</b>.
		 * 	While skirmishing Smaug or Gollum, Bilbo is strength +2.
		 * 	Each time a companion heals, you may spot a minion to draw a card.
		 */

		var scn = GetScenario();

		var card = scn.GetRingBearer();

		assertEquals("30_45", card.getBlueprintId());
		assertEquals("Bilbo", card.getBlueprint().getTitle());
		assertEquals("Master in Riddles", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BURGLAR));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void BilboIsStrength2MoreWhileSkirmishingGollumAndNotWhileHeWaitsForHim() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, runner}, new PhysicalCardImpl[]{bilbo, gollum});

		// Balin's skirmish first: Bilbo is assigned to Gollum and not yet skirmishing him.
		scn.FreepsResolveSkirmish(balin);
		assertEquals(3, scn.GetStrength(bilbo));
		scn.PassSkirmishActions();

		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(5, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BilboIsStrength2MoreWhileSkirmishingGollumHalfAWraithHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var gollum = scn.GetShadowCard("wraithgollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		assertEquals(3, scn.GetStrength(bilbo));
		scn.FreepsAssignToMinions(bilbo, gollum);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(5, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BilboIsStrength2MoreWhileSkirmishingSmaug() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var smaug = scn.GetShadowCard("smaug");

		// Erebor, site 7, is a mountain: Smaug stays there (he is discarded anywhere else, so he is laid there).
		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(smaug);

		scn.SkipToAssignments();
		assertEquals(3, scn.GetStrength(bilbo));
		scn.FreepsAssignToMinions(bilbo, smaug);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(5, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BilboIsNotStrongerSkirmishingAMinionWhoIsNoGollumNorIsAnotherCompanionSkirmishingGollum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, gollum}, new PhysicalCardImpl[]{bilbo, shelob});

		scn.FreepsResolveSkirmish(balin);
		assertEquals(7, scn.GetStrength(balin));
		assertEquals(3, scn.GetStrength(bilbo));
		// Balin 7 beats Gollum 5, who lives through it.
		scn.PassSkirmishActions();
		assertInZone(Zone.SHADOW_CHARACTERS, gollum);

		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(3, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ACompanionHealedWhileAMinionCanBeSpottedMayDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var oin = scn.GetFreepsCard("oin");
		var battlefury = scn.GetFreepsCard("battlefury");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(oin);
		scn.MoveCardsToHand(battlefury);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, oin);
		assertInZone(Zone.HAND, battlefury);
		scn.AddWoundsToChar(balin, 1);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(1, scn.GetWoundsOn(balin));
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(oin, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(balin);

		// Óin's own text heals Balin as he is transferred.
		assertAttachedTo(oin, balin);
		assertEquals(0, scn.GetWoundsOn(balin));
		assertEquals(0, scn.GetFreepsHandCount());

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsChooseOptionalTrigger(bilbo);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void EachHealOfBilboHimselfMakesTheOfferAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var oin = scn.GetFreepsCard("oin");
		var elrond = scn.GetFreepsCard("elrond");
		var battlefury = scn.GetFreepsCard("battlefury");
		var runner = scn.GetShadowCard("runner");
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(oin);
		scn.MoveCardsToHand(battlefury);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, oin);
		assertInZone(Zone.HAND, battlefury);
		scn.AddWoundsToChar(bilbo, 2);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetWoundsOn(bilbo));
		int deck = scn.GetFreepsDeckCount();
		// Óin's Aid: Bilbo, the one companion, bears him, and is healed.
		scn.FreepsAcceptOptionalTrigger();
		assertAttachedTo(oin, bilbo);
		assertEquals(1, scn.GetWoundsOn(bilbo));

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsChooseOptionalTrigger(bilbo);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());

		// The Runner is never assigned, and is in play in the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		assertInZone(Zone.SHADOW_CHARACTERS, runner);
		assertEquals(0, scn.GetWoundsOn(elrond));
		scn.FreepsUseCardAction(elrond);
		assertEquals(2, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetWoundsOn(bilbo));

		// Offered again, and declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AMinionOrAnAllyHealedMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var lindir = scn.GetFreepsCard("lindir");
		var shaman = scn.GetShadowCard("shaman");
		scn.MoveAlliesToTable(lindir);

		scn.StartGame();
		scn.MoveMinionsToTable(shaman);
		scn.AddWoundsToChar(shaman, 1);
		scn.AddWoundsToChar(lindir, 1);
		scn.SetTwilight(10);

		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(1, scn.GetWoundsOn(shaman));
		assertEquals(1, scn.GetWoundsOn(lindir));
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();

		// The Shadow player heals his own Uruk Shaman.
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(shaman));
		scn.ShadowUseCardAction(shaman);
		assertEquals(0, scn.GetWoundsOn(shaman));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The Shaman is never assigned, and is in play at the move from the regroup phase, where Lindir heals himself.
		scn.SkipToPhase(Phase.REGROUP);
		scn.PassCurrentPhaseActions();
		scn.PassReconcile(scn.P2());
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(lindir));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(lindir));
		assertInZone(Zone.SHADOW_CHARACTERS, shaman);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		assertEquals(hand, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionHealedWithNoMinionToSpotMakesNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();

		scn.StartGame();
		scn.AddWoundsToChar(bilbo, 1);
		// Rivendell, site 3, is a sanctuary: the turn that begins there heals.
		scn.SkipToSite(3);

		assertEquals(1, scn.GetWoundsOn(bilbo));
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		scn.FreepsChooseCard(bilbo);
		assertEquals(0, scn.GetWoundsOn(bilbo));

		// The one heal used, the turn opens on fellowship actions with nothing offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(hand, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
	}
}
