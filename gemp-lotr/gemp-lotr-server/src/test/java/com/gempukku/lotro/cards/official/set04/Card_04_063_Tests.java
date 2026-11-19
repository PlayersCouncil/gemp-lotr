package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("brooch1", "4_63");
					put("brooch2", "4_63");

					put("celeborn", "1_34");   // Celeborn, Lord of Lorien: Elf ally, cost 2; a fellowship ability used only on purpose
					put("arwen", "1_30");      // Arwen, Daughter of Elrond: Elf companion; +3 only against a Nazgul
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("gimli", "1_13");      // Gimli, Son of Gloin: Dwarf companion; a skirmish ability used only on purpose
					put("eowyn", "7_229");     // Eowyn, Restless Maiden: Skirmish: discard a possession borne by Eowyn to make her strength +2
					put("rsword", "1_112");    // Ranger's Sword: possession, bearer must be Aragorn; damage +1 only
					put("helm", "5_89");       // Rohirrim Helm: possession on a [rohan] Man, no number; a skirmish ability used only on purpose
					put("bow", "1_41");        // Elven Bow: possession, bearer must be an Elf; makes bearer an archer
					put("narsil", "9_34");     // Narsil, Blade of the Faithful: an artifact on a [gondor] Man; its response needs 2 [gondor] Men
					put("watch", "12_35");     // Watch and Wait: a Free Peoples condition on a companion; acts only when bearer wins

					put("beauty1", "1_205");   // Beauty Is Fading: Shadow event: exert a Nazgul to discard a Free Peoples possession or condition
					put("beauty2", "1_205");
					put("mmsword", "1_225");   // Sword of Minas Morgul: Shadow possession: Skirmish: exert Ulaire Lemenya to discard a possession borne by a character he is skirmishing
					put("lemenya", "1_232");   // Ulaire Lemenya, Lieutenant of Morgul: Nazgul, vitality 2; -2 only to bearers of [wraith] conditions
					put("cantea", "10_67");    // Ulaire Cantea, Thrall of The One: Nazgul, vitality 3; Skirmish: heal him to discard a possession borne by a character he is skirmishing
					put("ravage", "4_32");     // Ravage the Defeated: Response: if your [dunland] Man wins, discard all Free Peoples cards borne by the loser
					put("dsavage", "4_17");    // Dunlending Savage: [dunland] Man, strength 11, vitality 1, no text
					put("savage", "1_151");    // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1 only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElvenBroochStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Elven Brooch
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Brooch
		 * Game Text: To play, spot an Elf. Bearer must be a companion.<br><b>Response:</b> If another possession borne by bearer is about to be discarded by a Shadow card, discard this possession instead.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("brooch1");

		assertEquals("Elven Brooch", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.BROOCH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutAnElfAndOnceAnElfAllyIsPlayedGoesOnACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brooch1 = scn.GetFreepsCard("brooch1");
		var celeborn = scn.GetFreepsCard("celeborn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(brooch1, celeborn);

		scn.StartGame();
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(brooch1));

		scn.FreepsPlayCard(celeborn);
		assertInZone(Zone.SUPPORT, celeborn);
		assertTrue(scn.FreepsPlayAvailable(brooch1));
		scn.FreepsPlayCard(brooch1);
		// Companions of three races and cultures; not the Elf ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, gimli, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(celeborn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertAttachedTo(brooch1, aragorn);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnElfCompanionIsAnElfToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brooch1 = scn.GetFreepsCard("brooch1");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(arwen);
		scn.MoveCardsToHand(brooch1);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(brooch1));
		scn.FreepsPlayCard(brooch1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(arwen, scn.GetRingBearer()));
		scn.FreepsChooseCard(arwen);
		assertAttachedTo(brooch1, arwen);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenAShadowEventDiscardsAnotherPossessionOfBearerTheBroochIsDiscardedInstead() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brooch1 = scn.GetFreepsCard("brooch1");
		var brooch2 = scn.GetFreepsCard("brooch2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var rsword = scn.GetFreepsCard("rsword");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(aragorn, brooch1, rsword);
		scn.AttachCardsTo(arwen, brooch2);

		var beauty1 = scn.GetShadowCard("beauty1");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(beauty1);

		scn.StartGame();
		scn.SetTwilight(10);
		assertAttachedTo(brooch1, aragorn);
		assertAttachedTo(rsword, aragorn);
		assertAttachedTo(brooch2, arwen);
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.ShadowPlayCard(beauty1);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		scn.ShadowChooseCard(rsword);

		// The copy on Aragorn, who bears the sword, is offered; not the copy on Arwen.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brooch1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brooch2));
		scn.FreepsChooseOptionalTrigger(brooch1);

		assertInDiscard(brooch1);
		assertAttachedTo(rsword, aragorn);
		assertAttachedTo(brooch2, arwen);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DeclinedThePossessionIsDiscardedAndTheBroochStays() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brooch1 = scn.GetFreepsCard("brooch1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rsword = scn.GetFreepsCard("rsword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, brooch1, rsword);

		var beauty1 = scn.GetShadowCard("beauty1");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCardsToHand(beauty1);

		scn.StartGame();
		scn.SetTwilight(10);
		assertAttachedTo(brooch1, aragorn);
		assertAttachedTo(rsword, aragorn);
		scn.MoveMinionsToTable(lemenya);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(beauty1);
		scn.ShadowChooseCard(rsword);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brooch1));
		scn.FreepsDeclineOptionalTrigger();

		assertInDiscard(rsword);
		assertAttachedTo(brooch1, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AShadowPossessionsAbilityDiscardingAnotherPossessionOfBearerIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brooch1 = scn.GetFreepsCard("brooch1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rsword = scn.GetFreepsCard("rsword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, brooch1, rsword);

		var lemenya = scn.GetShadowCard("lemenya");
		var mmsword = scn.GetShadowCard("mmsword");

		scn.StartGame();
		assertAttachedTo(brooch1, aragorn);
		assertAttachedTo(rsword, aragorn);
		scn.MoveMinionsToTable(lemenya);
		scn.AttachCardsTo(lemenya, mmsword);

		scn.SkipToAssignments();
		assertAttachedTo(mmsword, lemenya);
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.ShadowUseCardAction(mmsword);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		// Both of Aragorn's possessions can be chosen.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rsword, brooch1));
		scn.ShadowChooseCard(rsword);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brooch1));
		scn.FreepsChooseOptionalTrigger(brooch1);
		assertInDiscard(brooch1);
		assertAttachedTo(rsword, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMinionsAbilityDiscardingAnotherPossessionOfBearerIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brooch1 = scn.GetFreepsCard("brooch1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rsword = scn.GetFreepsCard("rsword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, brooch1, rsword);

		var cantea = scn.GetShadowCard("cantea");

		scn.StartGame();
		assertAttachedTo(brooch1, aragorn);
		assertAttachedTo(rsword, aragorn);
		scn.MoveMinionsToTable(cantea);
		// A wound for him to heal as his ability's cost.
		scn.AddWoundsToChar(cantea, 1);

		scn.SkipToAssignments();
		assertEquals(1, scn.GetWoundsOn(cantea));
		scn.FreepsAssignToMinions(aragorn, cantea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		scn.ShadowUseCardAction(cantea);
		assertEquals(0, scn.GetWoundsOn(cantea));
		scn.ShadowChooseCard(rsword);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brooch1));
		scn.FreepsChooseOptionalTrigger(brooch1);
		assertInDiscard(brooch1);
		assertAttachedTo(rsword, aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void APossessionOfBearerDiscardedByAFreePeoplesCardIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brooch1 = scn.GetFreepsCard("brooch1");
		var eowyn = scn.GetFreepsCard("eowyn");
		var helm = scn.GetFreepsCard("helm");
		scn.MoveCompanionsToTable(eowyn);
		scn.AttachCardsTo(eowyn, brooch1, helm);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		assertAttachedTo(brooch1, eowyn);
		assertAttachedTo(helm, eowyn);
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, savage);
		scn.FreepsResolveSkirmish(eowyn);

		assertEquals(6, scn.GetStrength(eowyn));
		// Eowyn's own ability, a Free Peoples card, discards the Helm she bears.
		scn.FreepsUseCardAction(eowyn);
		scn.FreepsChooseCard(helm);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brooch1));
		assertInDiscard(helm);
		assertAttachedTo(brooch1, eowyn);
		assertEquals(8, scn.GetStrength(eowyn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void APossessionOfAnotherCompanionOrTheBroochItselfIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brooch1 = scn.GetFreepsCard("brooch1");
		var brooch2 = scn.GetFreepsCard("brooch2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var arwen = scn.GetFreepsCard("arwen");
		var rsword = scn.GetFreepsCard("rsword");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(aragorn, arwen);
		scn.AttachCardsTo(aragorn, brooch1, rsword);
		scn.AttachCardsTo(arwen, brooch2, bow);

		var beauty1 = scn.GetShadowCard("beauty1");
		var beauty2 = scn.GetShadowCard("beauty2");
		var lemenya = scn.GetShadowCard("lemenya");
		var cantea = scn.GetShadowCard("cantea");
		scn.MoveCardsToHand(beauty1, beauty2);

		scn.StartGame();
		scn.SetTwilight(15);
		assertAttachedTo(brooch1, aragorn);
		assertAttachedTo(rsword, aragorn);
		assertAttachedTo(brooch2, arwen);
		assertAttachedTo(bow, arwen);
		scn.MoveMinionsToTable(lemenya, cantea);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		// Arwen's bow: the copy she bears is offered, the one Aragorn bears is not.
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.ShadowPlayCard(beauty1);
		scn.ShadowChooseCard(lemenya);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		scn.ShadowChooseCard(bow);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(brooch2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brooch1));
		scn.FreepsDeclineOptionalTrigger();
		assertInDiscard(bow);
		assertAttachedTo(brooch1, aragorn);
		assertAttachedTo(brooch2, arwen);

		// The Brooch itself: no "another" possession, so neither copy answers.
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(cantea));
		scn.ShadowPlayCard(beauty2);
		// Lemenya is exhausted: Cantea exerts unasked.
		assertEquals(1, scn.GetWoundsOn(cantea));
		scn.ShadowChooseCard(brooch1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brooch1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brooch2));
		assertInDiscard(brooch1);
		assertAttachedTo(rsword, aragorn);
		assertAttachedTo(brooch2, arwen);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnArtifactOrAConditionOfBearerDiscardedByAShadowCardIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var brooch1 = scn.GetFreepsCard("brooch1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var watch = scn.GetFreepsCard("watch");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, brooch1, narsil, watch);

		var ravage = scn.GetShadowCard("ravage");
		var dsavage = scn.GetShadowCard("dsavage");
		scn.MoveCardsToHand(ravage);

		scn.StartGame();
		scn.SetTwilight(10);
		assertAttachedTo(brooch1, aragorn);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(watch, aragorn);
		scn.MoveMinionsToTable(dsavage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, dsavage);
		scn.FreepsResolveSkirmish(aragorn);
		// The Dunlending Savage (11) beats Aragorn (8).
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowPlayAvailable(ravage));
		scn.ShadowPlayCard(ravage);

		// An artifact and a condition of Aragorn's are about to be discarded with the Brooch: no other
		// possession of his is, and the Brooch is not offered.
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(brooch1));
		assertInDiscard(narsil);
		assertInDiscard(watch);
		assertInDiscard(brooch1);
	}
}
