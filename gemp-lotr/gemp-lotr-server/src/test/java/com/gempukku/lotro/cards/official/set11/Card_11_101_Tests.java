package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_101_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("breelander", "11_101");

					// Conditions that a character bears.
					// Wielding the Ring: [men] condition, cost 1, resistance -2.  "Bearer must be a companion
					// (except the Ring-bearer)."  Its "To play, spot a [men] minion" is about playing it.
					put("wielding", "11_105");
					// Stone Trolls: [shire] tale, cost 2.  "Plays on any character. Limit 1 per character."  Its
					// strength +3 is for a skirmish with a Troll or an Uruk-hai.
					put("trolls", "1_314");
					// Let Her Deal With Them: [gollum] condition, cost 1.  "Bearer must be a minion. Limit 1 per
					// bearer. Bearer is damage +1."
					put("deal", "7_63");
					// The Last Alliance of Elves and Men: [elven] tale.  "Bearer must be a [gondor] Man. Limit 1
					// per bearer."  Its strength is for each Elf, and there is none.
					put("alliance1", "1_49");
					put("alliance2", "1_49");
					// Lost in the Woods and Black Breath: [wraith] conditions that are played to the support area and
					// go to a character only by their own text, "Skirmish: Transfer this condition from your support
					// area to" a companion a Nazgul is skirmishing.
					put("lost", "11_212");
					put("breath", "1_207");

					// What is no condition borne by a character.
					put("harp", "18_3");        // Thorin's Harp: [dwarven] condition in the support area; its maneuver ability exerts a [dwarven] companion and is never used
					put("fear", "1_278");       // Strength Born of Fear: [sauron] condition in the support area; a skirmish ability for (3)
					put("brand", "2_32");       // Flaming Brand: a possession, "Bearer must be a Man"; its bonus is for a skirmish with a Nazgul
					// Saruman's Frost: [isengard] condition that a site bears.  Each Hobbit companion there is strength
					// -2, and it is discarded at the end of the turn.
					put("frost", "1_135");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man; a skirmish ability never used here
					put("faramir", "4_117");    // Faramir, Son of Denethor: a [gondor] Man; his text is about skirmishes involving him
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf; a skirmish ability never used here
					put("knight", "5_35");      // Gondorian Knight: a [gondor] Man; Knight is its whole text
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally; a skirmish ability never used here
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man, an ally; his maneuver ability needs initiative and 2 cards discarded from hand

					put("footman", "11_82");    // Footman of Dunland: [men] Man, strength 7, vitality 3; the current site gains plains
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai; Damage +1 is its whole text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwarthyBreelanderStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Swarthy Bree-lander
		 * Unique: False
		 * Side: Shadow
		 * Culture: Men
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: At the start of the maneuver phase, you may remove (2) to transfer a condition borne by a character to another eligible bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("breelander");

		assertEquals("Swarthy Bree-lander", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MEN, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(4, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void AtTheStartOfTheManeuverPhaseRemovesTwoToTransferAConditionACharacterBearsToAnotherEligibleBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		var bounder = scn.GetFreepsCard("bounder");
		var merchant = scn.GetFreepsCard("merchant");
		var trolls = scn.GetFreepsCard("trolls");
		var harp = scn.GetFreepsCard("harp");
		var brand = scn.GetFreepsCard("brand");
		scn.MoveCompanionsToTable(aragorn, gimli, knight);
		scn.MoveAlliesToTable(bounder, merchant);
		scn.MoveCardsToSupportArea(harp);
		scn.AttachCardsTo(bounder, trolls);

		var breelander = scn.GetShadowCard("breelander");
		var footman = scn.GetShadowCard("footman");
		var savage = scn.GetShadowCard("savage");
		var wielding = scn.GetShadowCard("wielding");
		var deal = scn.GetShadowCard("deal");
		var fear = scn.GetShadowCard("fear");
		var frost = scn.GetShadowCard("frost");
		var breath = scn.GetShadowCard("breath");
		scn.MoveCardsToSupportArea(fear);
		// The Brand could go to any other Man, the Knight, the Merchant, the Footman or the Bree-lander himself.
		scn.AttachCardsTo(aragorn, wielding, brand);
		scn.AttachCardsTo(gimli, breath);

		scn.StartGame();
		scn.MoveMinionsToTable(breelander, footman, savage);
		scn.AttachCardsTo(footman, deal);
		scn.FreepsPass();
		scn.AttachCardsTo(scn.GetCurrentSite(), frost);
		scn.SetTwilight(5);
		scn.ShadowPass();

		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(breelander));
		// 6 - 2 for Wielding the Ring
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(6, scn.GetResistance(gimli));
		assertEquals(5, scn.GetTwilight());

		scn.ShadowAcceptOptionalTrigger();
		assertEquals(3, scn.GetTwilight());

		// A Shadow condition on a companion, a Free Peoples one on an ally, a Shadow one on a minion.  Not the
		// conditions in the two support areas, the possession Aragorn bears or the condition on the site; and
		// not Black Breath, which goes only to a character skirmishing a Nazgul.
		assertTrue(scn.ShadowHasCardChoiceAvailable(wielding, trolls, deal));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(harp, fear, brand, frost, breath));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(wielding);

		// "Bearer must be a companion (except the Ring-bearer)", and another than Aragorn.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, knight));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, frodo, bounder, merchant, breelander, footman, savage));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertAttachedTo(wielding, gimli);
		assertEquals(6, scn.GetResistance(aragorn));
		assertEquals(4, scn.GetResistance(gimli));
		assertAttachedTo(brand, aragorn);
		assertAttachedTo(trolls, bounder);
		assertAttachedTo(deal, footman);
		// 2 for the Bree-lander, and nothing for the condition, whose own cost is 1.
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AConditionGoesToAnyKindOfCharacterItsTextAllowsAndOneThatNobodyElseMayBearIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var bounder = scn.GetFreepsCard("bounder");
		var merchant = scn.GetFreepsCard("merchant");
		var trolls = scn.GetFreepsCard("trolls");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(bounder, merchant);
		scn.AttachCardsTo(bounder, trolls);

		var breelander = scn.GetShadowCard("breelander");
		var footman = scn.GetShadowCard("footman");
		var wielding = scn.GetShadowCard("wielding");
		// The only other companion is the Ring-bearer, who may not bear it.
		scn.AttachCardsTo(aragorn, wielding);

		scn.StartGame();
		scn.MoveMinionsToTable(breelander, footman);
		scn.FreepsPass();
		scn.SetTwilight(2);
		scn.ShadowPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(breelander));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(0, scn.GetTwilight());

		// Stone Trolls is the one condition that can go anywhere, so the game is already asking where: to a
		// companion, the Ring-bearer, the other ally or a minion, and not back to the Bounder.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(wielding, trolls));
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, merchant, breelander, footman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bounder));
		scn.ShadowChooseCard(footman);

		// A Free Peoples condition of cost 2 on a minion, with nothing in the pool to pay for it.
		assertAttachedTo(trolls, footman);
		assertAttachedTo(wielding, aragorn);
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverTriggerTransfersAConditionToAnotherEligibleBearerButNotOneWithNoBearerDefinition() throws DecisionResultInvalidException, CardNotFoundException {
		// Lost in the Woods is played to the support area and reaches a companion only by its own skirmish
		// ability, so it has no "eligible bearer" at the start of a maneuver phase and is not to be chosen here.
		// (It once was, and the game then failed looking for somewhere to put it.)
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		var alliance1 = scn.GetFreepsCard("alliance1");
		var alliance2 = scn.GetFreepsCard("alliance2");
		scn.MoveCompanionsToTable(boromir, aragorn, faramir);
		scn.AttachCardsTo(boromir, alliance1);
		scn.AttachCardsTo(aragorn, alliance2);

		var breelander = scn.GetShadowCard("breelander");
		var lost = scn.GetShadowCard("lost");
		scn.AttachCardsTo(faramir, lost);

		scn.StartGame();
		scn.MoveMinionsToTable(breelander);
		scn.FreepsPass();
		scn.SetTwilight(10);
		scn.ShadowPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(breelander));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(8, scn.GetTwilight());

		assertTrue(scn.ShadowHasCardChoiceAvailable(alliance1, alliance2));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lost));
		scn.ShadowChooseCard(alliance1);

		// Boromir is its bearer and Aragorn already bears one ("Limit 1 per bearer"), so Faramir is the only
		// eligible bearer and nothing is asked.
		assertAttachedTo(alliance1, faramir);
		assertAttachedTo(alliance2, aragorn);
		assertAttachedTo(lost, faramir);
		assertEquals(8, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DecliningLeavesEverythingAsItWasAndNothingIsOfferedAtTheStartOfTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, gimli, knight);

		var breelander = scn.GetShadowCard("breelander");
		var wielding = scn.GetShadowCard("wielding");
		scn.AttachCardsTo(aragorn, wielding);

		scn.StartGame();
		scn.MoveMinionsToTable(breelander);
		scn.FreepsPass();
		scn.SetTwilight(5);
		scn.ShadowPass();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(breelander));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(5, scn.GetTwilight());
		assertAttachedTo(wielding, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// The start of the next phase is no start of a maneuver phase.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(Phase.ARCHERY, scn.GetCurrentPhase());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(5, scn.GetTwilight());
		assertAttachedTo(wielding, aragorn);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	@Test
	public void NotOfferedWithOneTwilightTokenInThePool() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, gimli, knight);

		var breelander = scn.GetShadowCard("breelander");
		var wielding = scn.GetShadowCard("wielding");
		scn.AttachCardsTo(aragorn, wielding);

		scn.StartGame();
		scn.MoveMinionsToTable(breelander);
		scn.FreepsPass();
		scn.SetTwilight(1);
		scn.ShadowPass();

		// Gimli and the Knight could each bear the condition; only the (2) are wanting.
		assertEquals(Phase.MANEUVER, scn.GetCurrentPhase());
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetTwilight());
		assertAttachedTo(wielding, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoConditionOnAnyCharacterTheOfferIsTakenAndPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var harp = scn.GetFreepsCard("harp");
		var brand = scn.GetFreepsCard("brand");
		scn.MoveCompanionsToTable(aragorn, knight);
		scn.MoveCardsToSupportArea(harp);
		scn.AttachCardsTo(aragorn, brand);

		var breelander = scn.GetShadowCard("breelander");
		var fear = scn.GetShadowCard("fear");
		var frost = scn.GetShadowCard("frost");
		scn.MoveCardsToSupportArea(fear);

		scn.StartGame();
		scn.MoveMinionsToTable(breelander);
		scn.FreepsPass();
		scn.AttachCardsTo(scn.GetCurrentSite(), frost);
		scn.SetTwilight(5);
		scn.ShadowPass();

		// A possession on a Man beside two other Men, a condition on the site and one in each support area:
		// nothing to transfer.  The offer has a price, so it is made all the same.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(breelander));
		scn.ShadowAcceptOptionalTrigger();

		assertEquals(3, scn.GetTwilight());
		assertAttachedTo(brand, aragorn);
		assertAttachedTo(frost, scn.GetCurrentSite());
		assertInZone(Zone.SUPPORT, harp);
		assertInZone(Zone.SUPPORT, fear);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
