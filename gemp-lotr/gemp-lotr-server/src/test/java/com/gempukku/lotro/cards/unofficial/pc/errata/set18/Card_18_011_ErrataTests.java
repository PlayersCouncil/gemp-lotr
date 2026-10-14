package com.gempukku.lotro.cards.unofficial.pc.errata.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_011_ErrataTests
{

	// Frodo is the Ring-bearer: the boards on which Galadriel is an ally, a plain companion, or not there at all.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ewer", "68_11");
					put("galadrielAlly", "1_45");       // Galadriel, Lady of Light: ally, strength 3, vitality 3; a start-of-turn heal of allies and a fellowship ability
					put("galadrielCompanion", "10_11"); // Galadriel, Lady Redeemed: companion, strength 3, resistance 6; her ability discards an [elven] event from hand, used only on purpose
					put("celebornAlly", "1_34");        // Celeborn, Lord of Lorien: ally, cost 2; a fellowship ability only
					put("celeborn", "13_11");           // Celeborn, The Wise: companion; his texts need a forest or a move in the regroup phase
					put("elrond", "1_40");              // Elrond, Lord of Rivendell: an [elven] ally who is neither Galadriel nor Celeborn
					put("arwen", "1_30");               // Arwen, Daughter of Elrond: [elven] Elf companion, strength 6, resistance 6; stronger only against a Nazgul
					put("defiance", "1_37");            // Defiance: [elven] Skirmish event, cost 1: make an Elf strength +2
					put("supplies", "18_10");           // Elven Supplies: [elven] condition that holds [elven] tokens; its skirmish ability needs 2 of them
					put("savage", "1_151");             // Uruk Savage: strength 5, vitality 3; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Galadriel, Bearer of Wisdom is the Ring-bearer: strength 3, vitality 3, resistance 3, and resistance +1 for
	// each [elven] companion while she bears The One Ring. The Ruling Ring makes her strength +1.
	protected VirtualTableScenario GetGaladrielScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ewer", "68_11");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: [elven] Elf companion, strength 6, resistance 6; stronger only against a Nazgul
					put("celeborn", "13_11");   // Celeborn, The Wise: a third Elf companion; his texts need a forest or a move in the regroup phase
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] companion, strength 8; a maneuver ability used only on purpose
					put("defiance", "1_37");    // Defiance: [elven] Skirmish event, cost 1: make an Elf strength +2
					put("defenses", "1_32");    // Border Defenses: [elven] Skirmish event, cost 1: make an Elf strength +2
					put("elfsong", "1_39");     // Elf-song: [elven] Fellowship event, cost 1: spot an Elf to remove a burden
					put("swordarm", "1_116");   // Swordarm of the White Tower: [gondor] Skirmish event, cost 0: make a [gondor] companion strength +2
					// Sword-wall: [elven] "Skirmish or Regroup" event, cost 2: spot 3 Elf companions to liberate a
					// site or exert a minion.
					put("swordwall", "4_84");
					put("supplies", "18_10");   // Elven Supplies: [elven] condition that holds [elven] tokens; its skirmish ability needs 2 of them
					put("strands", "9_22");     // Strands of Elven Hair: [elven] possession that holds [elven] tokens; a fellowship ability only
					put("shake", "8_13");       // Shake Off the Shadow: [elven] condition that can hold [elven] tokens; fellowship and archery abilities only
					put("wish", "8_8");         // Wish For Our Kinfolk: [dwarven] condition that holds [dwarven] tokens; its skirmish ability is used only on purpose
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GaladrielsSilverEwerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Galadriel's Silver Ewer
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Artifact
		 * Subtype: Support Area
		 * Game Text: To play, spot Galadriel or Celeborn. While Galadriel is the Ring-bearer, she is strength +1 and resistance +2.
		 * 	Each time you play an [elven] skirmish event, you may exert Galadriel to reinforce an [elven] token.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ewer");

		assertEquals("Galadriel's Silver Ewer", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilCelebornIsSpottedAndWithNoGaladrielToExertItOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ewer = scn.GetFreepsCard("ewer");
		var celebornAlly = scn.GetFreepsCard("celebornAlly");
		var elrond = scn.GetFreepsCard("elrond");
		var arwen = scn.GetFreepsCard("arwen");
		var defiance = scn.GetFreepsCard("defiance");
		var supplies = scn.GetFreepsCard("supplies");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(supplies);
		scn.AddTokensToCard(supplies, 1);
		scn.MoveCardsToHand(ewer, celebornAlly, defiance);

		scn.StartGame();

		// An [elven] ally and an [elven] companion, neither of them Galadriel or Celeborn.
		assertFalse(scn.FreepsPlayAvailable(ewer));

		scn.FreepsPlayCard(celebornAlly);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(ewer));

		scn.FreepsPlayCard(ewer);
		assertInZone(Zone.SUPPORT, ewer);

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		// An [elven] skirmish event, and a token to reinforce, but no Galadriel to exert.
		scn.FreepsPlayCard(defiance);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsChooseCard(arwen);
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(1, scn.GetCultureTokensOn(supplies));
		assertEquals(0, scn.GetWoundsOn(celebornAlly));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void CelebornTheCompanionIsSpottedToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ewer = scn.GetFreepsCard("ewer");
		var celeborn = scn.GetFreepsCard("celeborn");
		scn.MoveCompanionsToTable(celeborn);
		scn.MoveCardsToHand(ewer);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(ewer));

		scn.FreepsPlayCard(ewer);
		assertInZone(Zone.SUPPORT, ewer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GaladrielTheRingBearerIsSpottedToPlayAndIsThenStrengthPlus1AndResistancePlus2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var galadriel = scn.GetRingBearer();
		var ewer = scn.GetFreepsCard("ewer");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(ewer);

		scn.StartGame();
		scn.AddBurdens(1);

		// 3 + 1 from The Ruling Ring
		assertEquals(4, scn.GetStrength(galadriel));
		// 3 + 2 from her own text (two [elven] companions) - 1 for the burden
		assertEquals(4, scn.GetResistance(galadriel));
		assertEquals(6, scn.GetStrength(arwen));
		// 6 - 1 for the burden
		assertEquals(5, scn.GetResistance(arwen));
		assertTrue(scn.FreepsPlayAvailable(ewer));

		scn.FreepsPlayCard(ewer);
		assertInZone(Zone.SUPPORT, ewer);
		// 4 + 1 from the Ewer
		assertEquals(5, scn.GetStrength(galadriel));
		// 4 + 2 from the Ewer
		assertEquals(6, scn.GetResistance(galadriel));
		// "She": the other [elven] companion gains nothing.
		assertEquals(6, scn.GetStrength(arwen));
		assertEquals(5, scn.GetResistance(arwen));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AGaladrielWhoIsNotTheRingBearerIsSpottedToPlayAndNeitherSheNorTheRingBearerGainsAnything() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ewer = scn.GetFreepsCard("ewer");
		var galadriel = scn.GetFreepsCard("galadrielCompanion");
		scn.MoveCompanionsToTable(galadriel);
		scn.MoveCardsToHand(ewer);

		scn.StartGame();

		assertEquals(3, scn.GetStrength(galadriel));
		assertEquals(6, scn.GetResistance(galadriel));
		// Frodo, Son of Drogo: 3 + 1 from The Ruling Ring, resistance 10
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(10, scn.GetResistance(frodo));
		assertTrue(scn.FreepsPlayAvailable(ewer));

		scn.FreepsPlayCard(ewer);
		assertInZone(Zone.SUPPORT, ewer);
		// Galadriel is in play and is not the Ring-bearer; the Ring-bearer is not Galadriel.
		assertEquals(3, scn.GetStrength(galadriel));
		assertEquals(6, scn.GetResistance(galadriel));
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(10, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void GaladrielTheAllyIsSpottedToPlayAndExertsToReinforceButGainsNoStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ewer = scn.GetFreepsCard("ewer");
		var galadriel = scn.GetFreepsCard("galadrielAlly");
		var arwen = scn.GetFreepsCard("arwen");
		var defiance = scn.GetFreepsCard("defiance");
		var supplies = scn.GetFreepsCard("supplies");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(galadriel);
		scn.MoveCardsToSupportArea(supplies);
		scn.AddTokensToCard(supplies, 1);
		scn.MoveCardsToHand(ewer, defiance);

		scn.StartGame();

		assertEquals(3, scn.GetStrength(galadriel));
		assertTrue(scn.FreepsPlayAvailable(ewer));
		scn.FreepsPlayCard(ewer);
		assertInZone(Zone.SUPPORT, ewer);
		// She is no Ring-bearer.
		assertEquals(3, scn.GetStrength(galadriel));

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(1, scn.GetCultureTokensOn(supplies));

		scn.FreepsPlayCard(defiance);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ewer));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(2, scn.GetCultureTokensOn(supplies));

		// Defiance goes on to its own effect.
		scn.FreepsChooseCard(arwen);
		assertEquals(8, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void EachElvenSkirmishEventPlayedOffersToExertGaladrielToReinforceAnElvenToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var galadriel = scn.GetRingBearer();
		var ewer = scn.GetFreepsCard("ewer");
		var arwen = scn.GetFreepsCard("arwen");
		var defiance = scn.GetFreepsCard("defiance");
		var defenses = scn.GetFreepsCard("defenses");
		var supplies = scn.GetFreepsCard("supplies");
		var strands = scn.GetFreepsCard("strands");
		var shake = scn.GetFreepsCard("shake");
		var wish = scn.GetFreepsCard("wish");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(ewer, supplies, strands, shake, wish);
		scn.AddTokensToCard(supplies, 1);
		scn.AddTokensToCard(strands, 2);
		scn.AddTokensToCard(wish, 2);
		scn.MoveCardsToHand(defiance, defenses);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(galadriel));

		scn.FreepsPlayCard(defiance);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ewer));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(galadriel));

		// A condition and a possession that each hold an [elven] token; an [elven] card with no token and a
		// [dwarven] card with [dwarven] tokens have nothing to reinforce.
		assertTrue(scn.FreepsHasCardChoiceAvailable(supplies, strands));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(shake, wish));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(strands);
		assertEquals(3, scn.GetCultureTokensOn(strands));
		assertEquals(1, scn.GetCultureTokensOn(supplies));
		assertEquals(0, scn.GetCultureTokensOn(shake));
		assertEquals(2, scn.GetCultureTokensOn(wish));

		// Defiance goes on to its own effect.
		scn.FreepsChooseCard(arwen);
		assertEquals(8, scn.GetStrength(arwen));
		scn.ShadowPass();

		// A second [elven] skirmish event: asked again, and this time declined.
		scn.FreepsPlayCard(defenses);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ewer));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(3, scn.GetCultureTokensOn(strands));
		assertEquals(1, scn.GetCultureTokensOn(supplies));

		scn.FreepsChooseCard(arwen);
		assertEquals(10, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnElvenEventOfAnotherPhaseAndASkirmishEventOfAnotherCultureBringNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var galadriel = scn.GetRingBearer();
		var ewer = scn.GetFreepsCard("ewer");
		var arwen = scn.GetFreepsCard("arwen");
		var aragorn = scn.GetFreepsCard("aragorn");
		var elfsong = scn.GetFreepsCard("elfsong");
		var swordarm = scn.GetFreepsCard("swordarm");
		var defiance = scn.GetFreepsCard("defiance");
		var strands = scn.GetFreepsCard("strands");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen, aragorn);
		scn.MoveCardsToSupportArea(ewer, strands);
		scn.AddTokensToCard(strands, 2);
		scn.MoveCardsToHand(elfsong, swordarm, defiance);

		scn.StartGame();
		scn.AddBurdens(1);

		// An [elven] event that is a Fellowship event.
		scn.FreepsPlayCard(elfsong);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertInDiscard(elfsong);
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		// A Skirmish event that is a [gondor] one.
		scn.FreepsPlayCard(swordarm);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(10, scn.GetStrength(aragorn));
		assertInDiscard(swordarm);
		scn.ShadowPass();

		// On the same board an [elven] Skirmish event brings the offer.
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		scn.FreepsPlayCard(defiance);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ewer));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(3, scn.GetCultureTokensOn(strands));
		scn.FreepsChooseCard(arwen);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SwordWallPlayedInASkirmishIsAnElvenSkirmishEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var galadriel = scn.GetRingBearer();
		var ewer = scn.GetFreepsCard("ewer");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var swordwall = scn.GetFreepsCard("swordwall");
		var strands = scn.GetFreepsCard("strands");
		var savage = scn.GetShadowCard("savage");
		// Three Elf companions for Sword-wall: Galadriel, Arwen and Celeborn.
		scn.MoveCompanionsToTable(arwen, celeborn);
		scn.MoveCardsToSupportArea(ewer, strands);
		scn.AddTokensToCard(strands, 2);
		scn.MoveCardsToHand(swordwall);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(2, scn.GetCultureTokensOn(strands));

		scn.FreepsPlayCard(swordwall);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ewer));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(3, scn.GetCultureTokensOn(strands));
		// Sword-wall's own effect: no site to liberate, so the minion exerts.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SwordWallPlayedInTheRegroupPhaseIsNoSkirmishEventAndBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var galadriel = scn.GetRingBearer();
		var ewer = scn.GetFreepsCard("ewer");
		var arwen = scn.GetFreepsCard("arwen");
		var celeborn = scn.GetFreepsCard("celeborn");
		var swordwall = scn.GetFreepsCard("swordwall");
		var strands = scn.GetFreepsCard("strands");
		var savage = scn.GetShadowCard("savage");
		// Three Elf companions for Sword-wall: Galadriel, Arwen and Celeborn.
		scn.MoveCompanionsToTable(arwen, celeborn);
		scn.MoveCardsToSupportArea(ewer, strands);
		scn.AddTokensToCard(strands, 2);
		scn.MoveCardsToHand(swordwall);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);
		// Arwen (6) beats the Uruk Savage (5), which takes a wound and is there in the regroup phase for
		// Sword-wall to exert.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());

		// "Skirmish or Regroup": an action that may be played in more than one phase is of the type of the
		// phase it is taken in, so this is a regroup event.
		assertTrue(scn.FreepsPlayAvailable(swordwall));
		scn.FreepsPlayCard(swordwall);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		// Sword-wall's own effect: no site to liberate, so the minion exerts.
		assertEquals(2, scn.GetWoundsOn(savage));
		assertInDiscard(swordwall);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AnExhaustedGaladrielCannotExertSoNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var galadriel = scn.GetRingBearer();
		var ewer = scn.GetFreepsCard("ewer");
		var arwen = scn.GetFreepsCard("arwen");
		var defiance = scn.GetFreepsCard("defiance");
		var strands = scn.GetFreepsCard("strands");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(ewer, strands);
		scn.AddTokensToCard(strands, 2);
		scn.MoveCardsToHand(defiance);

		scn.StartGame();
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(galadriel, 2);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		scn.FreepsPlayCard(defiance);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		scn.FreepsChooseCard(arwen);
		assertEquals(8, scn.GetStrength(arwen));
		assertEquals(2, scn.GetWoundsOn(galadriel));
		assertEquals(2, scn.GetCultureTokensOn(strands));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoElvenTokenToReinforceTheOfferStandsAndGaladrielExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();

		var galadriel = scn.GetRingBearer();
		var ewer = scn.GetFreepsCard("ewer");
		var arwen = scn.GetFreepsCard("arwen");
		var defiance = scn.GetFreepsCard("defiance");
		var shake = scn.GetFreepsCard("shake");
		var wish = scn.GetFreepsCard("wish");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToSupportArea(ewer, shake, wish);
		scn.AddTokensToCard(wish, 2);
		scn.MoveCardsToHand(defiance);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(galadriel));

		// The offer has a cost, so it is made whatever the exertion will buy.
		scn.FreepsPlayCard(defiance);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ewer));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetCultureTokensOn(shake));
		assertEquals(2, scn.GetCultureTokensOn(wish));

		scn.FreepsChooseCard(arwen);
		assertEquals(8, scn.GetStrength(arwen));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
