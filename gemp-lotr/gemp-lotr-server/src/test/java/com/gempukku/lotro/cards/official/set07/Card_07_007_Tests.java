package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_007_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "7_7");
					put("farin", "1_11");         // Farin, Dwarven Emissary: Dwarf companion, vitality 3; +2 only against an Orc
					put("grimir", "1_17");        // Grimir, Dwarven Elder: Dwarf ally; a fellowship ability never used here
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: a companion who is no Dwarf
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: strength only

					// Stairs of Khazad-dum: [dwarven] tale in the support area; its text is about [moria] minions.
					put("stairs", "1_24");
					// Endurance of Dwarves: [dwarven] condition a Dwarf bears, vitality +1; a skirmish ability.
					put("endurance", "2_4");
					put("endurance2", "2_4");
					// The Choice of Luthien: [gondor] tale in the support area; a maneuver ability never used here.
					put("luthien", "1_100");
					put("axe", "1_9");            // Dwarven Axe: [dwarven] possession, hand weapon
					put("strike", "1_3");         // Axe Strike: [dwarven] skirmish event, kept in hand
					put("sleep", "1_84");         // Sleep, Caradhras: [gandalf] event. "Exert Gandalf to discard every condition."
					put("bilbo", "1_284");        // Bilbo, Retired Adventurer: [shire] ally; a fellowship ability never used here

					// Saruman's Power: [isengard] Shadow event. "Exert an [isengard] minion to discard all conditions."
					put("power", "1_136");
					// Saruman's Ambition: [isengard] condition in the support area; [isengard] events cost 1 less.
					put("ambition", "1_133");
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai; damage +1 is its whole text
					// Beauty Is Fading: [wraith] Maneuver event. "Exert a Nazgul to discard a Free Peoples possession or
					// Free Peoples condition."
					put("beauty1", "1_205");
					put("beauty2", "1_205");
					// Ulaire Nelya: Nazgul, vitality 3; fierce and damage +1 only at 2 burdens or 2 wounds on the
					// Ring-bearer.
					put("nelya", "2_84");
					// Why Shouldn't I Keep It?: [sauron] Maneuver event. "Spot a [sauron] minion to discard Bilbo. The
					// Free Peoples player may discard 2 Free Peoples conditions to prevent this."
					put("whyshould", "3_105");
					put("guard", "10_81");        // Cirith Ungol Guard: [sauron] Uruk-hai; damage +1 is its whole text
					// Ravage the Defeated: [dunland] Response event. "If your [dunland] Man wins a skirmish, discard all
					// Free Peoples cards borne by the companion or ally he was skirmishing."
					put("ravage", "4_32");
					put("brigand", "4_10");       // Dunlending Brigand: [dunland] Man, strength 8, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Buckland Homestead, a site of the Shadows block, is laid as site 2: "Shadow: Play a Nazgul to discard a
	// condition from play."
	protected VirtualTableScenario GetHomesteadScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "7_7");
					put("stairs", "1_24");        // Stairs of Khazad-dum: [dwarven] tale in the support area
					// Ulaire Lemenya, Lieutenant of Morgul: a Nazgul, cost 4, site number 3; her text needs a [wraith]
					// condition on a companion
					put("lemenya", "1_232");
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("homestead", "11_230");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Gimli, Feared Axeman
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: <b>Damage +1</b>.<br><b>Response:</b> If a [dwarven] condition is about to be discarded by an opponent, exert a Dwarf or discard a [dwarven] card from hand to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Feared Axeman", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void WhenTheShadowPlayerDiscardsADwarvenConditionAnyDwarfMayExertToPreventIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var grimir = scn.GetFreepsCard("grimir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stairs = scn.GetFreepsCard("stairs");
		var luthien = scn.GetFreepsCard("luthien");
		scn.MoveCompanionsToTable(gimli, farin, aragorn);
		scn.MoveAlliesToTable(grimir);
		scn.MoveCardsToSupportArea(stairs, luthien);

		var beauty = scn.GetShadowCard("beauty1");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, stairs, luthien);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(beauty);
		assertTrue(scn.ShadowHasCardChoiceAvailable(stairs, luthien));
		scn.ShadowChooseCard(stairs);

		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();

		// No [dwarven] card in hand: the exertion is the one cost there is, and it is not asked which.
		// Any Dwarf exerts, a companion other than Gimli and an ally among them; not a companion who is no Dwarf.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin, grimir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(grimir);

		assertEquals(1, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(farin));
		assertInZone(Zone.SUPPORT, stairs);
		assertInZone(Zone.SUPPORT, luthien);
		assertInZone(Zone.DISCARD, beauty);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithBothCostsPayableTheFreePeoplesPlayerChoosesToDiscardADwarvenCardFromHandToSaveABorneCondition() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var endurance = scn.GetFreepsCard("endurance");
		var strike = scn.GetFreepsCard("strike");
		var axe = scn.GetFreepsCard("axe");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, endurance);
		scn.MoveCardsToHand(strike, axe, sleep);

		var beauty = scn.GetShadowCard("beauty1");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		assertAttachedTo(endurance, gimli);
		assertInHand(strike, axe, sleep);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The condition Gimli bears is the one Free Peoples possession or condition, and is taken unasked.
		scn.ShadowPlayCard(beauty);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();

		// Gimli could exert and there are [dwarven] cards in hand: the player is asked which cost to pay.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert a Dwarf"));
		assertTrue(scn.FreepsChoiceAvailable("card from hand"));
		scn.FreepsChooseOption("card from hand");

		// A [dwarven] event or a [dwarven] possession; not the [gandalf] event.
		assertTrue(scn.FreepsHasCardChoiceAvailable(strike, axe));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sleep));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(axe);

		assertInDiscard(axe);
		assertInHand(strike, sleep);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertAttachedTo(endurance, gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoDwarfAbleToExertTheDwarvenCardIsDiscardedFromHandUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var stairs = scn.GetFreepsCard("stairs");
		var strike = scn.GetFreepsCard("strike");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveCardsToHand(strike);

		var beauty = scn.GetShadowCard("beauty1");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		// Vitality 3 each: both Dwarves are exhausted.
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(farin, 2);
		scn.MoveMinionsToTable(nelya);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(farin));
		assertInHand(strike);
		assertInZone(Zone.SUPPORT, stairs);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The Stairs are the one Free Peoples possession or condition, and are taken unasked.
		scn.ShadowPlayCard(beauty);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();

		// No choice of cost is asked, nor of the card: the one [dwarven] card in hand goes.
		assertInDiscard(strike);
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(2, scn.GetWoundsOn(farin));
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhenNoDwarfCanExertAndNoDwarvenCardIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var stairs = scn.GetFreepsCard("stairs");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveCardsToHand(sleep);

		var beauty = scn.GetShadowCard("beauty1");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		scn.AddWoundsToChar(gimli, 2);
		scn.MoveMinionsToTable(nelya);
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertInHand(sleep);
		assertInZone(Zone.SUPPORT, stairs);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The Stairs are the one Free Peoples possession or condition, and are taken unasked.
		scn.ShadowPlayCard(beauty);

		// Gimli is exhausted and the only card in hand is a [gandalf] one.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInDiscard(stairs);
		assertInHand(sleep);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ADwarvenPossessionOrAConditionOfAnotherCultureDiscardedByTheShadowPlayerBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var stairs = scn.GetFreepsCard("stairs");
		var luthien = scn.GetFreepsCard("luthien");
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, axe);
		scn.MoveCardsToSupportArea(stairs, luthien);

		var beauty1 = scn.GetShadowCard("beauty1");
		var beauty2 = scn.GetShadowCard("beauty2");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(beauty1, beauty2);

		scn.StartGame();
		scn.MoveMinionsToTable(nelya);
		assertAttachedTo(axe, gimli);
		assertInZone(Zone.SUPPORT, stairs, luthien);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.SetTwilight(15);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A [dwarven] possession.
		scn.ShadowPlayCard(beauty1);
		scn.ShadowChooseCard(axe);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInDiscard(axe);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());

		// A [gondor] condition.
		scn.FreepsPass();
		scn.ShadowPlayCard(beauty2);
		scn.ShadowChooseCard(luthien);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInDiscard(luthien);
		assertInZone(Zone.SUPPORT, stairs);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AgainstADiscardOfAllConditionsEachUseSavesOneDwarvenConditionAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var stairs = scn.GetFreepsCard("stairs");
		var endurance = scn.GetFreepsCard("endurance");
		var luthien = scn.GetFreepsCard("luthien");
		var strike = scn.GetFreepsCard("strike");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, endurance);
		scn.MoveCardsToSupportArea(stairs, luthien);
		scn.MoveCardsToHand(strike);

		var power = scn.GetShadowCard("power");
		var ambition = scn.GetShadowCard("ambition");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(ambition);
		scn.MoveCardsToHand(power);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertAttachedTo(endurance, gimli);
		assertInZone(Zone.SUPPORT, stairs, luthien, ambition);
		assertInHand(strike);
		assertEquals(0, scn.GetWoundsOn(farin));
		scn.FreepsPass();

		scn.ShadowPlayCard(power);
		assertEquals(1, scn.GetWoundsOn(savage));

		// Two [dwarven] conditions are about to go with the others. The first use exerts Farin and saves one of
		// them, chosen from the two (Ketura on Q-0313: the one card).
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseOption("Exert a Dwarf");
		assertTrue(scn.FreepsHasCardChoiceAvailable(gimli, farin));
		scn.FreepsChooseCard(farin);
		assertEquals(1, scn.GetWoundsOn(farin));
		assertTrue(scn.FreepsHasCardChoiceAvailable(stairs, endurance));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(luthien, ambition));
		scn.FreepsChooseCard(stairs);

		// The other is still about to go, and the response is offered again; this time the [dwarven] card in hand
		// pays.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseOption("card from hand");
		assertInDiscard(strike);

		assertInZone(Zone.SUPPORT, stairs);
		assertAttachedTo(endurance, gimli);
		assertInDiscard(luthien, ambition);
		assertEquals(1, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AgainstADiscardOfAllThatABeatenDwarfBearsEachUseSavesOneDwarvenConditionAndNotTheDwarvenPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var endurance = scn.GetFreepsCard("endurance");
		var endurance2 = scn.GetFreepsCard("endurance2");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(farin, axe, endurance, endurance2);

		var ravage = scn.GetShadowCard("ravage");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToHand(ravage);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand);
		assertAttachedTo(axe, farin);
		assertAttachedTo(endurance, farin);
		assertAttachedTo(endurance2, farin);
		assertEquals(0, scn.GetWoundsOn(farin));
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(farin, brigand);
		scn.FreepsResolveSkirmish(farin);

		// 5 + 2 for the axe against 8: Farin loses with one wound, and the Brigand has won.
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(farin));
		assertTrue(scn.ShadowPlayAvailable(ravage));
		scn.ShadowPlayCard(ravage);

		// A [dwarven] possession and two [dwarven] conditions are about to go together. No [dwarven] card is in
		// hand, so the exertion is the cost, and it is not asked which.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The first use saves one condition, chosen from the two; the possession is no condition.
		assertTrue(scn.FreepsHasCardChoiceAvailable(endurance, endurance2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(axe));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(endurance);
		assertAttachedTo(endurance, farin);

		// The other condition is still about to go, and the response is offered again; declined, it goes with the axe.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsDeclineOptionalTrigger();

		assertAttachedTo(endurance, farin);
		assertInDiscard(endurance2, axe);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ADwarvenConditionItsOwnerDiscardsBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var stairs = scn.GetFreepsCard("stairs");
		var sleep = scn.GetFreepsCard("sleep");
		scn.MoveCompanionsToTable(gimli, gandalf);
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveCardsToHand(sleep);

		scn.StartGame();

		assertInZone(Zone.SUPPORT, stairs);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(gandalf));

		scn.FreepsPlayCard(sleep);
		assertEquals(1, scn.GetWoundsOn(gandalf));

		// The Free Peoples player discards every condition himself: Gimli can exert, and nothing is offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInDiscard(stairs);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void DwarvenConditionsTheFreePeoplesPlayerChoosesToDiscardForAShadowEventBringNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var bilbo = scn.GetFreepsCard("bilbo");
		var stairs = scn.GetFreepsCard("stairs");
		var luthien = scn.GetFreepsCard("luthien");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(bilbo);
		scn.MoveCardsToSupportArea(stairs, luthien);

		var whyshould = scn.GetShadowCard("whyshould");
		var guard = scn.GetShadowCard("guard");
		scn.MoveCardsToHand(whyshould);

		scn.StartGame();
		scn.MoveMinionsToTable(guard);
		assertInZone(Zone.SUPPORT, bilbo, stairs, luthien);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(whyshould);

		// The Free Peoples player may let Bilbo go; he chooses to discard two of his own conditions instead, and so
		// is the one discarding them (Ketura on Q-0171).
		assertTrue(scn.FreepsDecisionAvailable("discard 2 FP conditions"));
		scn.FreepsChooseYes();
		// They are his only two conditions, and go unasked.

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInDiscard(stairs, luthien);
		assertInZone(Zone.SUPPORT, bilbo);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ADwarvenConditionTheShadowPlayerDiscardsByASitesTextMayBeSaved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHomesteadScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var stairs = scn.GetFreepsCard("stairs");
		var lemenya = scn.GetShadowCard("lemenya");
		var homestead = scn.GetShadowSite("homestead");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToSupportArea(stairs);
		scn.MoveCardsToHand(lemenya);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, stairs);
		assertInHand(lemenya);
		assertEquals(0, scn.GetWoundsOn(gimli));
		scn.MoveCardToAdventurePath(homestead);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertSame(homestead, scn.GetCurrentSite());

		// The discard is the Shadow player's own, by a card that is of neither side.
		// The Stairs are the one condition in play, and are taken unasked.
		scn.ShadowUseCardAction(homestead);
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();

		// Gimli is the one Dwarf and the hand is empty: he exerts unasked.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertInZone(Zone.SUPPORT, stairs);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
