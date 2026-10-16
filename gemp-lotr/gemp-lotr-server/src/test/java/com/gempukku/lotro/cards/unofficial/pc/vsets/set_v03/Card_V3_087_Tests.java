package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static com.gempukku.lotro.framework.Assertions.assertInZone;
import static org.junit.Assert.*;

public class Card_V3_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Two dots: two copies may be in play.
					put("halifirien1", "103_87");
					put("halifirien2", "103_87");
					// War-beacon: a [gondor] beacon, cost 1, to play spot an unbound Man; fellowship abilities never used here
					put("war1", "103_35");
					put("war2", "103_35");
					put("war3", "103_35");
					put("calenhad", "103_86");   // Northern Signal-fire, Flame of Calenhad: a [rohan] beacon; a maneuver ability never used here
					put("catapult", "8_32");     // Catapult: a [gondor] possession in the support area that is no beacon; a maneuver ability never used here

					put("eowyn", "5_122");       // Eowyn, Daughter of Eomund: a [rohan] Man, strength 6, vitality 3; a maneuver ability never used here
					put("rider", "4_286");       // Rider of Rohan: a [rohan] Man, strength 5, vitality 3. Skirmish: if mounted, exert him to exert a minion skirmishing an unbound companion
					put("boromir", "1_97");      // Boromir, Son of Denethor: a [gondor] Man, strength 7; skirmish: exert him to make a Hobbit stronger
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: an unbound Man for the War-beacon; a maneuver ability never used here
					put("knight", "8_39");       // Knight of Dol Amroth: a [gondor] Man who is a knight; his text needs him to win a skirmish
					put("pippin", "103_31");     // Pippin, Trollslayer: a knight who is a Hobbit; a skirmish ability never used here
					put("haldir", "1_48");       // Haldir, Elf of the Golden Wood: strength 5, vitality 3; stronger only at sites 6 to 8
					put("barliman", "1_70");     // Barliman Butterbur: a Man who is an ally, vitality 2; a fellowship ability never used here

					// Rider's Mount: a [rohan] mount, cost 1, for a [rohan] Man. Each minion skirmishing bearer exerts at the start of the skirmish
					put("ridermount1", "4_287");
					put("ridermount2", "4_287");
					// Horse of Rohan: a mount, cost 2, for a Man, an Elf or a Wizard, with the same text
					put("horse1", "4_283");
					put("horse2", "4_283");
					put("brego", "4_263");       // Brego: the same again, cost 1, and "When you play Brego, you may heal bearer"
					// Knight's Mount: a [gondor] mount, cost 1, for a knight; its text needs bearer to win a skirmish
					put("knightmount1", "8_40");
					put("knightmount2", "8_40");
					put("chain", "4_278");       // Heavy Chain: a [rohan] possession, cost 1, for a Man, that is no mount; it is about being overwhelmed
					// Banner of the Mark: a [rohan] hand weapon with no strength. Each time bearer wins a skirmish, you may
					// exert a minion; and a response to the same win that discards it
					put("banner", "6_90");

					// Gollum, Dark as Darkness: strength 5, vitality 4; his abilities need a burden or two exertions of his own
					put("gollum", "9_28");
					put("troop", "1_177");       // Goblin Patrol Troop: strength 13, vitality 3, no text
					put("grima", "4_154");       // Grima, Wormtongue: strength 4, vitality 3; his maneuver ability needs a companion bearing 3 cards
					put("nomad", "7_132");       // Desert Nomad: a Southron, a Man who is a minion, vitality 2; damage +1 only with 2 threats
					put("mumak", "5_73");        // Mumak: a Shadow mount, cost 2, that a Southron bears; makes its bearer fierce
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Towers path: an ally may be assigned to a skirmish only at his home site, and the [rohan] allies who are Men
	// have theirs at site 4 of this path.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halifirien", "103_87");
					put("war", "103_35");        // War-beacon: a [gondor] beacon; fellowship abilities never used here
					// Guma, Plains Farmer: a [rohan] Man who is an ally, home site 4, strength 4, vitality 2; a fellowship ability never used here
					put("guma", "4_277");
					put("ridermount", "4_287");  // Rider's Mount: each minion skirmishing bearer exerts at the start of the skirmish
					put("gollum", "9_28");       // Gollum, Dark as Darkness: strength 5, vitality 4; his abilities need a burden or two exertions of his own
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NorthernSignalfireStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Northern Signal-fire, Flame of Halifirien
		 * Unique: 2
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Beacon. To play, hinder 2 beacons.
		 * 	Each time you play a mount on your Man, you may add (1) to heal that Man.
		 * 	Response: If a mount borne by your Man exerts a minion, hinder a beacon to exert that minion again.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("halifirien1");

		assertEquals("Northern Signal-fire", card.getBlueprint().getTitle());
		assertEquals("Flame of Halifirien", card.getBlueprint().getSubtitle());
		assertEquals(2, card.getBlueprint().getUniqueRestriction());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.BEACON));
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedUntilTwoBeaconsCanBeHinderedAndThenCostsTwoAndHindersBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien1");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var calenhad = scn.GetFreepsCard("calenhad");
		var catapult = scn.GetFreepsCard("catapult");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(war1, calenhad, catapult);
		// A hindered beacon is no beacon while it is hindered, and the Catapult never was one.
		scn.HinderCard(calenhad);
		scn.MoveCardsToHand(halifirien, war2);

		scn.StartGame();

		// One beacon that can be hindered: one short.
		assertFalse(scn.FreepsPlayAvailable(halifirien));

		scn.FreepsPlayCard(war2);

		// 1 for the War-beacon
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(halifirien));
		assertFalse(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(war2));

		scn.FreepsPlayCard(halifirien);

		// 1 + 2 for the Signal-fire. With exactly two beacons to hinder nothing is asked.
		assertEquals(3, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, halifirien);
		assertTrue(scn.IsHindered(war1));
		assertTrue(scn.IsHindered(war2));
		assertTrue(scn.IsHindered(calenhad));
		assertFalse(scn.IsHindered(catapult));
		assertFalse(scn.IsHindered(halifirien));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheTwoBeaconsHinderedToPlayAreChosenFromUnhinderedBeaconsOfAnyCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien1");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var war3 = scn.GetFreepsCard("war3");
		var calenhad = scn.GetFreepsCard("calenhad");
		var catapult = scn.GetFreepsCard("catapult");
		scn.MoveCardsToSupportArea(war1, war2, war3, calenhad, catapult);
		scn.HinderCard(war3);
		scn.MoveCardsToHand(halifirien);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		scn.FreepsPlayCard(halifirien);

		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		// Two [gondor] beacons and a [rohan] one
		assertTrue(scn.FreepsHasCardChoiceAvailable(war1, war2, calenhad));
		// A beacon already hindered, a [gondor] support possession that is no beacon, and the card being played
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(war3, catapult, halifirien));

		scn.FreepsChooseCards(war2, calenhad);

		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, halifirien);
		assertTrue(scn.IsHindered(war2));
		assertTrue(scn.IsHindered(calenhad));
		assertFalse(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(catapult));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMountPlayedOnAManOfYoursOffersToAddOneAndHealHimAndNoOtherPossessionOrBearerDoes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien1");
		var eowyn = scn.GetFreepsCard("eowyn");
		var rider = scn.GetFreepsCard("rider");
		var boromir = scn.GetFreepsCard("boromir");
		var haldir = scn.GetFreepsCard("haldir");
		var chain = scn.GetFreepsCard("chain");
		var horse = scn.GetFreepsCard("horse1");
		var ridermount = scn.GetFreepsCard("ridermount1");
		scn.MoveCompanionsToTable(eowyn, rider, boromir, haldir);
		scn.MoveCardsToSupportArea(halifirien);
		scn.MoveCardsToHand(chain, horse, ridermount);
		scn.AddWoundsToChar(eowyn, 2);
		scn.AddWoundsToChar(rider, 2);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(haldir, 1);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());

		// A possession that is no mount, on a wounded Man
		scn.FreepsPlayCard(chain);
		scn.FreepsChooseCard(boromir);

		assertAttachedTo(chain, boromir);
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A mount, on a wounded Elf
		scn.FreepsPlayCard(horse);
		scn.FreepsChooseCard(haldir);

		assertAttachedTo(horse, haldir);
		// 1 + 2 for the Horse of Rohan
		assertEquals(3, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A mount, on a wounded Man
		scn.FreepsPlayCard(ridermount);
		scn.FreepsChooseCard(eowyn);

		assertAttachedTo(ridermount, eowyn);
		// 3 + 1 for the Rider's Mount
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien));

		scn.FreepsAcceptOptionalTrigger();

		assertEquals(5, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(eowyn));
		// Another wounded [rohan] Man, and the two the first plays were made on
		assertEquals(2, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheOfferIsMadeForEachMountPlayedOnACompanionOrAllyWhoIsAManWoundedOrNotAndMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien1");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var rider = scn.GetFreepsCard("rider");
		var barliman = scn.GetFreepsCard("barliman");
		var knightmount1 = scn.GetFreepsCard("knightmount1");
		var knightmount2 = scn.GetFreepsCard("knightmount2");
		var ridermount = scn.GetFreepsCard("ridermount1");
		var horse = scn.GetFreepsCard("horse1");
		scn.MoveCompanionsToTable(knight, pippin, rider);
		scn.MoveAlliesToTable(barliman);
		scn.MoveCardsToSupportArea(halifirien);
		scn.MoveCardsToHand(knightmount1, knightmount2, ridermount, horse);
		scn.AddWoundsToChar(pippin, 1);
		scn.AddWoundsToChar(rider, 2);
		scn.AddWoundsToChar(barliman, 1);

		scn.StartGame();

		// A mount on a knight who is a Hobbit
		scn.FreepsPlayCard(knightmount1);
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(knightmount1, pippin);
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [gondor] mount on a [gondor] Man with no wound. The only knight without a mount: nothing is asked.
		scn.FreepsPlayCard(knightmount2);

		assertAttachedTo(knightmount2, knight);
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien));

		scn.FreepsAcceptOptionalTrigger();

		// (1) is added though there is nothing to heal on him, and nobody else is healed.
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(2, scn.GetWoundsOn(rider));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A second mount in the same phase, on the only [rohan] Man
		scn.FreepsPlayCard(ridermount);

		assertAttachedTo(ridermount, rider);
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien));

		scn.FreepsDeclineOptionalTrigger();

		assertEquals(4, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(rider));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Horse of Rohan on the one Man left without a mount, who is an ally
		scn.FreepsPlayCard(horse);

		assertAttachedTo(horse, barliman);
		// 4 + 2 for the Horse of Rohan
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien));

		scn.FreepsAcceptOptionalTrigger();

		assertEquals(7, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(2, scn.GetWoundsOn(rider));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TwoCopiesEachMakeTheirOwnOfferForOneMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien1 = scn.GetFreepsCard("halifirien1");
		var halifirien2 = scn.GetFreepsCard("halifirien2");
		var eowyn = scn.GetFreepsCard("eowyn");
		var ridermount = scn.GetFreepsCard("ridermount1");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(halifirien1, halifirien2);
		scn.MoveCardsToHand(ridermount);
		scn.AddWoundsToChar(eowyn, 2);

		scn.StartGame();

		scn.FreepsPlayCard(ridermount);

		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien2));

		scn.FreepsChooseOptionalTrigger(halifirien1);

		assertEquals(2, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(halifirien1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien2));

		scn.FreepsChooseOptionalTrigger(halifirien2);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BregoPlayedOnAManBringsItsOwnOfferToHealAndThisCardsAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien1");
		var eowyn = scn.GetFreepsCard("eowyn");
		var brego = scn.GetFreepsCard("brego");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(halifirien);
		scn.MoveCardsToHand(brego);
		scn.AddWoundsToChar(eowyn, 2);

		scn.StartGame();

		scn.FreepsPlayCard(brego);

		assertAttachedTo(brego, eowyn);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brego));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien));

		scn.FreepsChooseOptionalTrigger(brego);

		// Brego heals for nothing.
		assertEquals(1, scn.GetTwilight());
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien));

		scn.FreepsChooseOptionalTrigger(halifirien);

		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AMountPlayedInTheManeuverPhaseAsTheCostOfAnAbilityBringsTheOfferToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien1");
		var eowyn = scn.GetFreepsCard("eowyn");
		var ridermount = scn.GetFreepsCard("ridermount1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(halifirien);
		scn.MoveCardsToHand(ridermount);
		scn.AddWoundsToChar(eowyn, 2);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToPhase(Phase.MANEUVER);

		int pool = scn.GetTwilight();

		// Eowyn's own text: "Maneuver: Play a [rohan] possession on Eowyn to heal her." The Rider's Mount is the only one in hand.
		scn.FreepsUseCardAction(eowyn);

		assertAttachedTo(ridermount, eowyn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien));

		scn.FreepsAcceptOptionalTrigger();

		// 1 for the Rider's Mount and 1 added; one wound healed by her ability and one by this card.
		assertEquals(pool + 2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheOtherPlayerPlayingAMountOnHisOwnManBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien1");
		var eowyn = scn.GetFreepsCard("eowyn");
		var nomad = scn.GetShadowCard("nomad");
		var mumak = scn.GetShadowCard("mumak");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(halifirien);
		scn.MoveCardsToHand(mumak);
		scn.AddWoundsToChar(eowyn, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(nomad);
		scn.AddWoundsToChar(nomad, 1);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		int pool = scn.GetTwilight();

		// The Desert Nomad is a Man, and the only Southron: nothing is asked.
		scn.ShadowPlayCard(mumak);

		assertAttachedTo(mumak, nomad);
		assertEquals(pool - 2, scn.GetTwilight());
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetWoundsOn(nomad));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ResponseHindersABeaconToExertAgainAMinionThatAMountOnAManOfYoursExertedAsOftenAsBeaconsAllow() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien1");
		var war1 = scn.GetFreepsCard("war1");
		var war2 = scn.GetFreepsCard("war2");
		var calenhad = scn.GetFreepsCard("calenhad");
		var catapult = scn.GetFreepsCard("catapult");
		var rider = scn.GetFreepsCard("rider");
		var ridermount = scn.GetFreepsCard("ridermount1");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, ridermount);
		scn.MoveCardsToSupportArea(halifirien, war1, war2, calenhad, catapult);
		scn.HinderCard(war2);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, gollum);

		assertEquals(0, scn.GetWoundsOn(gollum));

		scn.FreepsResolveSkirmish(rider);

		// The Rider's Mount has exerted Gollum.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien));

		scn.FreepsChooseOptionalTrigger(halifirien);

		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		// This card, a [gondor] beacon and another [rohan] one
		assertTrue(scn.FreepsHasCardChoiceAvailable(halifirien, war1, calenhad));
		// A beacon already hindered, and a [gondor] support possession that is no beacon
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(war2, catapult));

		scn.FreepsChooseCard(war1);

		assertTrue(scn.IsHindered(war1));
		assertFalse(scn.IsHindered(halifirien));
		assertFalse(scn.IsHindered(calenhad));
		assertEquals(2, scn.GetWoundsOn(gollum));
		// The same exertion may be answered again.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien));

		scn.FreepsChooseOptionalTrigger(halifirien);
		scn.FreepsChooseCard(halifirien);

		// Hindered as its own cost, it still exerts the minion.
		assertTrue(scn.IsHindered(halifirien));
		assertFalse(scn.IsHindered(calenhad));
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(rider));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMountOnAnElfBringsNoResponseAndOneOnAManOfAnotherCultureDoesAndItMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien1");
		var war1 = scn.GetFreepsCard("war1");
		var haldir = scn.GetFreepsCard("haldir");
		var boromir = scn.GetFreepsCard("boromir");
		var horse1 = scn.GetFreepsCard("horse1");
		var horse2 = scn.GetFreepsCard("horse2");
		var grima = scn.GetShadowCard("grima");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(haldir, boromir);
		scn.AttachCardsTo(haldir, horse1);
		scn.AttachCardsTo(boromir, horse2);
		scn.MoveCardsToSupportArea(halifirien, war1);

		scn.StartGame();
		scn.MoveMinionsToTable(grima, gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{haldir, grima}, new PhysicalCardImpl[]{boromir, gollum});
		scn.FreepsResolveSkirmish(haldir);

		// Haldir's Horse of Rohan has exerted Grima, and nothing is offered for it.
		assertEquals(1, scn.GetWoundsOn(grima));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Haldir's 5 beats Grima's 4.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(0, scn.GetWoundsOn(gollum));

		scn.FreepsResolveSkirmish(boromir);

		// Boromir's Horse of Rohan has exerted Gollum.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien));

		scn.FreepsDeclineOptionalTrigger();

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertFalse(scn.IsHindered(halifirien));
		assertFalse(scn.IsHindered(war1));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMountedMansOwnAbilityExertingAMinionBringsNoResponse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien1");
		var war1 = scn.GetFreepsCard("war1");
		var eowyn = scn.GetFreepsCard("eowyn");
		var rider = scn.GetFreepsCard("rider");
		var ridermount = scn.GetFreepsCard("ridermount1");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(eowyn, rider);
		scn.AttachCardsTo(rider, ridermount);
		scn.MoveCardsToSupportArea(halifirien, war1);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, gollum);
		scn.FreepsResolveSkirmish(eowyn);

		// Eowyn has no mount, so nothing has exerted Gollum yet.
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The Rider of Rohan, who is mounted, exerts to exert the minion skirmishing Eowyn: he does it, not his mount.
		scn.FreepsUseCardAction(rider);

		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(halifirien));
		assertFalse(scn.IsHindered(war1));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ABannerBorneByAMountedManExertingAMinionBringsNoResponseWhereHisMountsExertionDid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien1");
		var war1 = scn.GetFreepsCard("war1");
		var eowyn = scn.GetFreepsCard("eowyn");
		var ridermount = scn.GetFreepsCard("ridermount1");
		var banner = scn.GetFreepsCard("banner");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(eowyn);
		scn.AttachCardsTo(eowyn, ridermount, banner);
		scn.MoveCardsToSupportArea(halifirien, war1);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, gollum);
		scn.FreepsResolveSkirmish(eowyn);

		// Her Rider's Mount has exerted Gollum, and the response is offered for that.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien));
		scn.FreepsDeclineOptionalTrigger();

		// Eowyn's 6 beats Gollum's 5: a wound for losing.
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(2, scn.GetWoundsOn(gollum));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "Exert a minion"));

		scn.FreepsChooseOptionalTrigger(banner, "Exert a minion");

		// The Banner, which the same Man bears and which is no mount, has exerted Gollum: nothing is offered for it.
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(halifirien));
		assertFalse(scn.IsHindered(halifirien));
		assertFalse(scn.IsHindered(war1));
		// What is left to answer is the Banner's own response to the win.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(banner, "liberate"));
	}

	@Test
	public void AMountBorneByAManWhoIsAnAllyBringsTheResponseAtHisHomeSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var halifirien = scn.GetFreepsCard("halifirien");
		var war = scn.GetFreepsCard("war");
		var guma = scn.GetFreepsCard("guma");
		var ridermount = scn.GetFreepsCard("ridermount");
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveAlliesToTable(guma);
		scn.AttachCardsTo(guma, ridermount);
		scn.MoveCardsToSupportArea(halifirien, war);
		scn.MoveMinionsToTable(gollum);
		// The fellowship moves to site 4, Guma's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guma, gollum);

		assertEquals(0, scn.GetWoundsOn(gollum));

		scn.FreepsResolveSkirmish(guma);

		// Guma's Rider's Mount has exerted Gollum.
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien));

		scn.FreepsChooseOptionalTrigger(halifirien);
		scn.FreepsChooseCard(war);

		assertTrue(scn.IsHindered(war));
		assertFalse(scn.IsHindered(halifirien));
		assertEquals(2, scn.GetWoundsOn(gollum));

		scn.FreepsDeclineOptionalTrigger();

		assertEquals(2, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithTwoMinionsExertedByTheMountOneUseExertsOneOfThemAgainAndNotTheOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halifirien = scn.GetFreepsCard("halifirien1");
		var war1 = scn.GetFreepsCard("war1");
		var rider = scn.GetFreepsCard("rider");
		var ridermount = scn.GetFreepsCard("ridermount1");
		var gollum = scn.GetShadowCard("gollum");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(rider);
		scn.AttachCardsTo(rider, ridermount);
		scn.MoveCardsToSupportArea(halifirien, war1);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(rider, gollum);
		scn.ShadowAssignToMinions(rider, troop);
		scn.FreepsResolveSkirmish(rider);

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien));

		// One offer is made for each minion, and the two read alike: this takes the first of them.
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(war1);

		assertTrue(scn.IsHindered(war1));
		// One of the two has been exerted again and the other has not.
		assertEquals(3, scn.GetWoundsOn(gollum) + scn.GetWoundsOn(troop));
		assertTrue(scn.GetWoundsOn(gollum) >= 1);
		assertTrue(scn.GetWoundsOn(troop) >= 1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(halifirien));
	}
}
