package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_205_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beauty", "1_205");

					put("lemenya", "1_232");    // Úlairë Lemenya: [wraith] Nazgûl, strength 9, vitality 2; her text weakens only bearers of a [wraith] condition
					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, vitality 3; his bonuses need 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc and no Nazgûl, vitality 2; its bonus needs 2 wounds on the Ring-bearer
					put("poison", "8_28");      // Spider Poison: a Shadow possession in its owner's support area; its ability needs Shelob
					put("breath", "1_207");     // Black Breath: a Shadow condition in its owner's support area; its ability is a skirmish one

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, Ring-bound and not the Ring-bearer
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a [dwarven] ally; his maneuver ability is used only on purpose
					put("armor", "1_92");       // Armor: [gondor] possession borne by a Man; caps his wounds in skirmishes only
					put("toby", "1_305");       // Old Toby: [shire] possession in the support area; its draw is offered only as it is played
					put("saga", "1_114");       // The Saga of Elendil: [gondor] condition borne by a [gondor] companion; a skirmish ability
					put("moria", "1_21");       // Lord of Moria: [dwarven] condition in the support area; makes Dwarves damage +1
					put("shards", "3_44");      // The Shards of Narsil: a Free Peoples ARTIFACT in the support area; fellowship abilities only
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeautyIsFadingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Beauty Is Fading
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 5
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert a Nazgûl to discard a Free Peoples possession or Free Peoples condition. If you can spot no such card, discard an ally or companion (except the Ring-bearer) instead.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beauty");

		assertEquals("Beauty Is Fading", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(5, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsANazgulOfAnyCultureToDiscardAFreePeoplesPossessionOrConditionAndNoCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beauty = scn.GetShadowCard("beauty");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var hound = scn.GetShadowCard("hound");
		var poison = scn.GetShadowCard("poison");
		var breath = scn.GetShadowCard("breath");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var armor = scn.GetFreepsCard("armor");
		var toby = scn.GetFreepsCard("toby");
		var saga = scn.GetFreepsCard("saga");
		var moria = scn.GetFreepsCard("moria");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.AttachCardsTo(aragorn, armor, saga);
		scn.MoveCardsToSupportArea(toby, moria, shards);
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, khamul, hound);
		scn.MoveCardsToSupportArea(poison, breath);
		scn.SetTwilight(10);
		assertAttachedTo(armor, aragorn);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, toby, moria, shards, poison, breath);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowPlayAvailable(beauty));
		scn.ShadowPlayCard(beauty);

		// The cost: a Nazgûl of either culture; a [wraith] Orc is no Nazgûl.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, khamul));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(khamul));

		// The target: Free Peoples possessions and conditions, borne and in the support area, of four cultures. Not the
		// Free Peoples artifact, nor the Shadow possession or condition, nor any character.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armor, toby, saga, moria));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(shards, poison, breath, aragorn, sam, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(armor);

		assertInDiscard(armor, beauty);
		assertAttachedTo(saga, aragorn);
		assertInZone(Zone.SUPPORT, toby, moria, shards, poison, breath);
		// With such a card to spot, no companion is discarded.
		assertInPlay(aragorn, sam, frodo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesPossessionOrConditionToSpotItDiscardsAnAllyOrCompanionButNotTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beauty = scn.GetShadowCard("beauty");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var poison = scn.GetShadowCard("poison");
		var breath = scn.GetShadowCard("breath");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var thrarin = scn.GetFreepsCard("thrarin");
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		scn.MoveCardsToSupportArea(poison, breath);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, thrarin, shards, poison, breath);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.ShadowPlayCard(beauty);
		// Lemenya is the one Nazgûl, and exerts unasked.
		assertEquals(1, scn.GetWoundsOn(lemenya));

		// The Free Peoples artifact and the Shadow possession and condition are no such card, so the second half
		// applies: a companion of either culture (Sam Ring-bound) or the ally; not the Ring-bearer, nor a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam, thrarin));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, lemenya, hound, shards, poison, breath));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sam);

		assertInDiscard(sam, beauty);
		assertNotInZone(Zone.DEAD, sam);
		assertInPlay(aragorn, thrarin, frodo, shards);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithOnlyTheRingBearerAndNothingElseToDiscardItIsPlayedAndItsNazgulExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beauty = scn.GetShadowCard("beauty");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		var shards = scn.GetFreepsCard("shards");
		scn.MoveCardsToSupportArea(shards);
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, shards);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowPlayAvailable(beauty));
		scn.ShadowPlayCard(beauty);

		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInDiscard(beauty);
		assertInPlay(frodo, shards);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithOnlyAFreePeoplesPossessionLeftItIsDiscardedAndNoCompanionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beauty = scn.GetShadowCard("beauty");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToSupportArea(toby);
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, toby);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.ShadowPlayCard(beauty);

		// Lemenya exerts unasked, and the one possession is the card spotted: it is discarded and no companion is asked for.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInDiscard(toby, beauty);
		assertInPlay(aragorn, sam, frodo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithOnlyAFreePeoplesConditionLeftItIsDiscardedAndNoCompanionIs() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beauty = scn.GetShadowCard("beauty");
		var lemenya = scn.GetShadowCard("lemenya");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var moria = scn.GetFreepsCard("moria");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToSupportArea(moria);
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, moria);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.ShadowPlayCard(beauty);

		// Lemenya exerts unasked, and the one condition is the card spotted: it is discarded and no companion is asked for.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInDiscard(moria, beauty);
		assertInPlay(aragorn, sam, frodo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheOnlyNazgulIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beauty = scn.GetShadowCard("beauty");
		var lemenya = scn.GetShadowCard("lemenya");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, armor);
		scn.MoveCardsToHand(beauty);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya, hound);
		// Vitality 2 with 1 wound: exhausted. The [wraith] Orc beside her could exert, and is no Nazgûl.
		scn.AddWoundsToChar(lemenya, 1);
		scn.SetTwilight(10);
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(hound));
		assertAttachedTo(armor, aragorn);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertFalse(scn.ShadowPlayAvailable(beauty));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
