package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_11_196_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("foes", "11_196");
					put("foes2", "11_196");
					put("foes3", "11_196");
					put("foes4", "11_196");

					put("bloodthirsty", "11_178");  // Bloodthirsty Uruk: [uruk-hai] Uruk-hai, cost 3; strength +3 while an exhausted
					                                // companion can be spotted (none is here)
					put("saruman", "17_116");   // Saruman, Master of the White Hand: [uruk-hai] Wizard minion, no Uruk-hai; each
					                            // companion resistance -1, a skirmish ability used only on purpose
					put("cuguard", "10_81");    // Cirith Ungol Guard: [sauron] Uruk-hai, damage +1 and no other text
					put("worthy", "12_160");    // Worthy of Mordor: [uruk-hai] condition, support area; its trigger is at the
					                            // start of the regroup phase only
					put("defense", "1_244");    // Desperate Defense of the Ring: a [sauron] condition on a companion; its only
					                            // text answers its bearer being assigned to a skirmish

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, unbound; a maneuver ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf, unbound; a skirmish ability used only on purpose
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man, unbound, Knight and no other text
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer; abilities used only on purpose
					put("bounder", "1_286");    // Bounder: Hobbit ally; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OurFoesAreWeakStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Our Foes Are Weak
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype:
		 * Strength: -1
		 * Resistance: -3
		 * Game Text: To play, spot an [uruk-hai] minion. Bearer must be an unbound companion. Limit 1 per bearer.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("foes");

		assertEquals("Our Foes Are Weak", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(-1, card.getBlueprint().getStrength());
		assertEquals(-3, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedUntilAnUrukHaiMinionIsSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foes = scn.GetShadowCard("foes");
		var bloodthirsty = scn.GetShadowCard("bloodthirsty");
		var cuguard = scn.GetShadowCard("cuguard");
		var worthy = scn.GetShadowCard("worthy");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(foes, bloodthirsty);
		scn.MoveCardsToSupportArea(worthy);
		assertInZone(Zone.SUPPORT, worthy);

		scn.StartGame();
		scn.MoveMinionsToTable(cuguard);
		assertInZone(Zone.SHADOW_CHARACTERS, cuguard);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// A [sauron] Uruk-hai and an [uruk-hai] condition are in play; neither is an [uruk-hai] minion.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, cuguard);
		assertInZone(Zone.SUPPORT, worthy);
		assertFalse(scn.ShadowPlayAvailable(foes));

		scn.ShadowPlayCard(bloodthirsty);
		assertInZone(Zone.SHADOW_CHARACTERS, bloodthirsty);
		assertTrue(scn.ShadowPlayAvailable(foes));

		// Aragorn is the only unbound companion, and takes it without a choice.
		scn.ShadowPlayCard(foes);
		assertAttachedTo(foes, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithSarumanTheOnlyUrukHaiMinionItIsPlayedOnAChosenUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foes = scn.GetShadowCard("foes");
		var saruman = scn.GetShadowCard("saruman");
		var cuguard = scn.GetShadowCard("cuguard");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(foes);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman, cuguard);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Saruman is an [uruk-hai] minion who is no Uruk-hai: the spot is met all the same.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(foes));

		scn.ShadowPlayCard(foes);
		// Unbound companions of two cultures and races; not the Ring-bearer, nor Sam (Ring-bound), nor an ally,
		// nor a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, bounder, saruman, cuguard));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertAttachedTo(foes, gimli);
		assertNotAttachedTo(foes, aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ACompanionWhoBearsOneIsNotOfferedASecondAndWithEveryUnboundCompanionBearingOneNoneCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var foes = scn.GetShadowCard("foes");
		var foes2 = scn.GetShadowCard("foes2");
		var foes3 = scn.GetShadowCard("foes3");
		var foes4 = scn.GetShadowCard("foes4");
		var defense = scn.GetShadowCard("defense");
		var bloodthirsty = scn.GetShadowCard("bloodthirsty");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(aragorn, gimli, knight);
		scn.AttachCardsTo(aragorn, foes);
		// Gimli already bears a different Shadow condition, which is not a copy of this card.
		scn.AttachCardsTo(gimli, defense);
		scn.MoveCardsToHand(foes2, foes3, foes4);
		assertAttachedTo(foes, aragorn);
		assertAttachedTo(defense, gimli);

		scn.StartGame();
		scn.MoveMinionsToTable(bloodthirsty);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertAttachedTo(foes, aragorn);
		assertAttachedTo(defense, gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(foes2));

		scn.ShadowPlayCard(foes2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, knight));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		scn.ShadowChooseCard(gimli);
		assertAttachedTo(foes2, gimli);
		assertAttachedTo(defense, gimli);

		// The Knight is now the only unbound companion bearing none, and is given the third without a choice.
		scn.ShadowPlayCard(foes3);
		assertAttachedTo(foes3, knight);

		// The [uruk-hai] minion is still there and the pool still holds enough; there is no bearer left.
		assertTrue(scn.GetTwilight() >= 2);
		assertFalse(scn.ShadowPlayAvailable(foes4));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
